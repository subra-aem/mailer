package org.subra.aem.mailer.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subra.aem.commons.constants.SubraHttpType;
import org.subra.aem.commons.constants.SubraUserMapperService;
import org.subra.aem.commons.exceptions.SubraCustomException;
import org.subra.aem.commons.helpers.SubraCommonHelper;
import org.subra.aem.commons.jcr.constants.SubraJcrFileNames;
import org.subra.aem.commons.jcr.constants.SubraJcrPrimaryType;
import org.subra.aem.commons.jcr.constants.SubraJcrProperties;
import org.subra.aem.commons.jcr.utils.SubraResourceUtils;
import org.subra.aem.commons.utils.SubraStringUtils;
import org.subra.aem.mailer.EmailRequest;
import org.subra.aem.mailer.Template;
import org.subra.aem.mailer.internal.helpers.MailerHelper;
import org.subra.aem.mailer.services.MailerService;
import org.subra.aem.mailer.services.TemplateService;
import org.subra.aem.mailer.utils.MailerUtils;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * @author Raghava Joijode
 *
 */
@Component(service = TemplateService.class, immediate = true)
@ServiceRanking(60000)
@ServiceDescription("Subra Mailer Template Service")
@Designate(ocd = TemplateServiceImpl.Config.class)
public final class TemplateServiceImpl implements TemplateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateServiceImpl.class);

	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private MailerService mailerService;

	private ResourceResolver resolver;

	private Resource approvedTemplatesResource;
	private Resource draftTemplatesResource;
	private String draftTemplatesIDPrefix;
	private String approvedTemplatesIDPrefix;

	@ObjectClassDefinition(name = "Subra Mailer Template Service Configuration")
	public @interface Config {

		@AttributeDefinition(name = "Approved Templates Path")
		String approved_templates_path() default MailerUtils.DEFAULT_APPROVED_TEMPLATES_PATH;

		@AttributeDefinition(name = "Draft Templates Path")
		String draft_templates_path() default MailerUtils.DEFAULT_DRAFT_TEMPLATES_PATH;

		@AttributeDefinition(name = "Draft Templates ID Prefix")
		String draft_templates_id_prefix() default MailerUtils.DEFAULT_DRAFT_TEMPLATES_ID_PREFIX;

		@AttributeDefinition(name = "Approved Templates ID Prefix")
		String approved_templates_id_prefix() default MailerUtils.DEFAULT_APPROVED_TEMPLATES_ID_PREFIX;
	}

	@Activate
	protected void activate(final Config config) {
		try {
			resolver = resolverFactory
					.getServiceResourceResolver(SubraResourceUtils.getAuthInfo(SubraUserMapperService.EMAIL_SERVICE));
			draftTemplatesResource = SubraResourceUtils.getOrCreateResource(resolver, config.draft_templates_path(),
					SubraJcrPrimaryType.SLING_FOLDER);
			approvedTemplatesResource = SubraResourceUtils.getOrCreateResource(resolver,
					config.approved_templates_path(), SubraJcrPrimaryType.SLING_FOLDER);

		} catch (LoginException e) {
			LOGGER.error("Unable to get resource resolver...");
		}
		draftTemplatesIDPrefix = config.draft_templates_id_prefix();
		approvedTemplatesIDPrefix = config.approved_templates_id_prefix();
		MailerHelper.setTemplateIDPrefixes(draftTemplatesIDPrefix, approvedTemplatesIDPrefix);
	}

	@Override
	public List<Template> listTemplates() {
		List<Template> templates = new ArrayList<>();
		Iterator<Resource> approvedTemplatesItr = approvedTemplatesResource.listChildren();
		Iterator<Resource> draftTemplatesItr = draftTemplatesResource.listChildren();
		while (approvedTemplatesItr.hasNext()) {
			Template template = createTemplate(approvedTemplatesItr.next());
			template.setDraft(false);
			templates.add(template);
		}
		while (draftTemplatesItr.hasNext()) {
			templates.add(new Template(draftTemplatesItr.next()));
		}
		return templates;
	}

	private Template createTemplate(Resource resource) {
		Template template = new Template(resource);
		template.setLookUps(getLookUpKeys(template));
		return template;
	}

	@Override
	public Template getTemplate(final String id) throws SubraCustomException {
		StringBuilder pathBuilder = new StringBuilder();
		if (StringUtils.startsWith(id, approvedTemplatesIDPrefix))
			pathBuilder.append(approvedTemplatesResource.getPath()).append(SubraStringUtils.SLASH)
					.append(StringUtils.stripStart(id, approvedTemplatesIDPrefix));

		else if (StringUtils.startsWith(id, draftTemplatesIDPrefix))
			pathBuilder.append(draftTemplatesResource.getPath()).append(SubraStringUtils.SLASH)
					.append(StringUtils.stripStart(id, draftTemplatesIDPrefix));

		return Optional.of(resolver).map(r -> r.getResource(pathBuilder.toString())).map(this::createTemplate)
				.orElseThrow(() -> new SubraCustomException("Invalid template ID..."));
	}

	@Override
	public EmailRequest createOrUpdateTemplate(final String fileTitle, final String content) {
		try {
			Session session = SubraResourceUtils.adoptToOrThrow(resolver, Session.class);
			InputStream contentIS = IOUtils.toInputStream(content, SubraHttpType.CHARSET_UTF_8.value());
			Binary binaryContent = session.getValueFactory().createBinary(contentIS);
			Node rootNode = SubraResourceUtils.adoptToOrThrow(draftTemplatesResource, Node.class);
			final String fileName = SubraCommonHelper.createNameFromTitle(fileTitle);
			Node fileNode = null;
			if (!rootNode.hasNode(fileName)) {
				Node fileFolder = rootNode.addNode(fileName, JcrResourceConstants.NT_SLING_FOLDER);
				fileFolder.setProperty(SubraJcrProperties.PN_CREATED_BY.property(), session.getUserID());
				fileFolder.setProperty(SubraJcrProperties.PN_LOCKED.property(), false);
				fileNode = fileFolder.addNode(SubraJcrFileNames.DEFAULT_TEXT_FILE.value(), JcrConstants.NT_FILE);
				Node fileContent = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
				fileContent.setProperty(JcrConstants.JCR_MIMETYPE, SubraHttpType.MEDIA_TYPE_TEXT.value());
				fileContent.setProperty(JcrConstants.JCR_DATA, binaryContent);
			} else {
				fileNode = rootNode.getNode(fileName).getNode(SubraJcrFileNames.DEFAULT_TEXT_FILE.value());
				fileNode.getNode(JcrConstants.JCR_CONTENT).setProperty(JcrConstants.JCR_DATA, binaryContent);
			}
			session.save();
			return generateRequestFormat(new Template(resolver.getResource(fileNode.getParent().getPath())));
		} catch (IOException | RepositoryException e) {
			LOGGER.error("Error creating/updating template... ", e);
		} catch (Exception e) {
			LOGGER.error("Uncatched Error creating/updating template... ", e);
		}
		return null;
	}

	@Override
	public String readTemplate(final Template template) {
		return Optional.ofNullable(template).map(Template::getMessage).orElseThrow();
	}

	@Override
	public boolean deleteTemplate(final Template template) {
		boolean status = false;
		try {
			Resource templateResource = template.getResource();
			templateResource.getResourceResolver().delete(templateResource);
			status = true;
		} catch (PersistenceException e) {
			LOGGER.error("Error deleting template...", e);
		}
		return status;
	}

	@Override
	public List<String> getLookUpKeys(final Template template) {
		return SubraStringUtils.getLookUpKeys(readTemplate(template));
	}

	@Override
	public EmailRequest generateRequestFormat(final Template template) {
		EmailRequest emailRequest = new EmailRequest();
		emailRequest.setTemplateId(MailerHelper.getTemplateId(template));
		Map<String, String> params = new HashMap<>();
		params.put(MailerUtils.TO, "<to-emails-seperate-by-comma>");
		params.put(MailerUtils.CC, "<cc-emails-seperate-by-comma>");
		params.put(MailerUtils.BCC, "<bcc-emails-seperate-by-comma>");
		params.put(MailerUtils.SUBJECT, "<subject-line>");
		params.putAll(getLookUpKeys(template).stream().distinct().collect(Collectors.toMap(k -> k, v -> "<value>")));
		emailRequest.setParams(params);
		return emailRequest;
	}

	@Override
	public String generateEmailMarkUp(final EmailRequest email) {
		try {
			return MailerHelper.getEmailContent(readTemplate(getTemplate(email.getTemplateId())), email.getParams());
		} catch (SubraCustomException e) {
			LOGGER.error("Error generating email markup...", e);
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Map<String, Object> sendEmail(final EmailRequest email) {
		Template template;
		try {
			template = getTemplate(email.getTemplateId());
			return mailerService.sendEmail(template, email.getParams(), null, null);
		} catch (SubraCustomException e) {
			LOGGER.error("Error sending email...", e);
		}
		return Collections.emptyMap();
	}

}
