package org.subra.aem.mailer.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.component.propertytypes.ServiceVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subra.aem.commons.constants.SubraHttpType;
import org.subra.aem.commons.exceptions.SubraCustomException;
import org.subra.aem.commons.helpers.SubraCommonHelper;
import org.subra.aem.mailer.EmailRequest;
import org.subra.aem.mailer.Template;
import org.subra.aem.mailer.services.TemplateService;

/**
 * Sling Servlet Filter Api for templates
 */
@Component
@SlingServletFilter(scope = SlingServletFilterScope.REQUEST, pattern = MailerApiFilter.PATTERN, methods = HttpConstants.METHOD_GET)
@ServiceDescription("Subra Mailer Template Api")
@ServiceRanking(-700)
@ServiceVendor("Subra")
public class MailerApiFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailerApiFilter.class);

	protected static final String PATTERN = "/api/subra/mailer/v1/(?<action>create|generate-json|read-content|view)(?:/(?<tid>[a-zA-Z0-9-_%]+))?";
	private static final String TEMPLATE_ID_ERROR = "Template ID Error...";
	private static final String PATTERN_ERROR = "PATTERN_ERROR";
	private static final String FAILURE = "FAILURE";
	private static final String RS_FAILURE_REASON = "REASON";
	private static final String RS_STATUS = "STATUS";

	@Reference
	TemplateService templateService;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {
		final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
		Pattern pattern = Pattern.compile(PATTERN);
		Matcher matcher = pattern.matcher(slingRequest.getRequestURI());
		if (matcher.matches()) {
			final String action = matcher.group("action");
			final String id = StringUtils.defaultString(matcher.group("tid"), StringUtils.EMPTY);
			switch (action) {
			case "create":
				processCreateOrUpdateTemplate(slingRequest, response);
				break;
			case "generate-json":
				processGenerateRequestFormat(response, id);
				break;
			case "read-content":
				processReadTemplate(response, id);
				break;
			case "view":
				processListTemplates(response, id);
				break;
			default:
				processFailure(response, PATTERN_ERROR);
			}
		} else {
			processFailure(response, PATTERN_ERROR);
		}

		LOGGER.debug("request for {}, with selector {}", slingRequest.getRequestPathInfo().getResourcePath(),
				slingRequest.getRequestPathInfo().getSelectorString());

	}

	private void processListTemplates(final ServletResponse response, final String id) throws IOException {
		response.setContentType(SubraHttpType.MEDIA_TYPE_JSON.value());
		try {
			response.getWriter().write(SubraCommonHelper.writeValueAsString(
					id.equalsIgnoreCase("all") ? templateService.listTemplates() : templateService.getTemplate(id)));
		} catch (SubraCustomException e) {
			processFailure(response, TEMPLATE_ID_ERROR);
		}
	}

	private void processCreateOrUpdateTemplate(final SlingHttpServletRequest request, final ServletResponse response)
			throws IOException {
		final String fileTitle = request.getParameter("title");
		final String content = request.getParameter("content");
		EmailRequest emailRequest = templateService.createOrUpdateTemplate(fileTitle, content);
		response.setContentType(SubraHttpType.MEDIA_TYPE_JSON.value());
		response.getWriter().write(emailRequest.toString());
	}

	private void processReadTemplate(final ServletResponse response, final String id) throws IOException {
		response.setContentType(SubraHttpType.MEDIA_TYPE_TEXT.value());
		try {
			response.getWriter().write(templateService.readTemplate(getTemplate(id)));
		} catch (SubraCustomException e) {
			processFailure(response, TEMPLATE_ID_ERROR);
		}
	}

	private void processGenerateRequestFormat(final ServletResponse response, final String id) throws IOException {
		response.setContentType(SubraHttpType.MEDIA_TYPE_JSON.value());
		try {
			response.getWriter().write(templateService.generateRequestFormat(getTemplate(id)).toString());
		} catch (SubraCustomException e) {
			processFailure(response, TEMPLATE_ID_ERROR);
		}
	}

	private Template getTemplate(final String id) throws SubraCustomException {
		return templateService.getTemplate(id);
	}

	private void processFailure(final ServletResponse response, final String message) throws IOException {
		response.setContentType(SubraHttpType.MEDIA_TYPE_JSON.value());
		Map<String, String> result = new HashMap<>();
		result.put(RS_STATUS, FAILURE);
		result.put(RS_FAILURE_REASON, message);
		response.getWriter().write(SubraCommonHelper.writeValueAsString(result));
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		LOGGER.debug("MailerApiFilter initialised...");
	}

	@Override
	public void destroy() {
		LOGGER.debug("MailerApiFilter destroyed...");
	}

}