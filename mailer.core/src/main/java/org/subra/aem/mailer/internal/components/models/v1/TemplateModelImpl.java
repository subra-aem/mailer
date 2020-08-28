package org.subra.aem.mailer.internal.components.models.v1;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subra.aem.commons.exceptions.SubraCustomException;
import org.subra.aem.mailer.Template;
import org.subra.aem.mailer.models.TemplateModel;
import org.subra.aem.mailer.services.TemplateService;

/**
 * @author Raghava Joijode
 * 
 *         Implementation of TemplateList, to read template message and other
 *         attributes.
 */
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = { TemplateModel.class })
public class TemplateModelImpl implements TemplateModel {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateModelImpl.class);

	@Self
	private SlingHttpServletRequest request;

	@RequestAttribute
	private String templatePath;

	@Reference
	private TemplateService templateService;

	@RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
	private String id;

	private Template template;

	@PostConstruct
	protected void init() {
		try {
			template = templateService.getTemplate(id);
		} catch (SubraCustomException e) {
			LOGGER.error("Error reading template...", e);
		}
	}

	@Override
	public Template getTemplate() {
		return template;
	}

	@Override
	public String getMessage() {
		return template != null ? templateService.readTemplate(template) : StringUtils.EMPTY;
	}

	@Override
	public List<String> getLookUpKeys() {
		return template != null ? templateService.getLookUpKeys(template) : Collections.emptyList();
	}

}
