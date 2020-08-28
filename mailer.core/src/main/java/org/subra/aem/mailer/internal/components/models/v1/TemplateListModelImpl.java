package org.subra.aem.mailer.internal.components.models.v1;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subra.aem.commons.helpers.SubraCommonHelper;
import org.subra.aem.mailer.Template;
import org.subra.aem.mailer.models.TemplateListModel;
import org.subra.aem.mailer.services.TemplateService;

/**
 * @author Raghava Joijode
 * 
 *         Implementation of TemplateListModel, to list out all templates.
 */
@Model(adaptables = { SlingHttpServletRequest.class, Resource.class }, adapters = { TemplateListModel.class })
public class TemplateListModelImpl implements TemplateListModel {

	@Self
	private SlingHttpServletRequest request;

	@Reference
	private TemplateService templateService;

	@RequestAttribute
	private String templatePath;

	List<Template> templates;

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateListModelImpl.class);

	@PostConstruct
	protected void init() {
		templates = SubraCommonHelper.getCacheData(request, "templates", () -> templateService.listTemplates());
		LOGGER.debug("TemplateListModel initialized...");
	}

	@Override
	public List<Template> getTemplates() {
		return templates;
	}

}
