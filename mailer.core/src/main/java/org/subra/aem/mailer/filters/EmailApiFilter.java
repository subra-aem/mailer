package org.subra.aem.mailer.filters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.collections4.CollectionUtils;
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
import org.subra.aem.commons.helpers.RequestParser;
import org.subra.aem.commons.helpers.SubraCommonHelper;
import org.subra.aem.mailer.EmailRequest;
import org.subra.aem.mailer.services.TemplateService;

/**
 * Sling Servlet Filter Api for Sending Email
 */
@Component
@SlingServletFilter(scope = SlingServletFilterScope.REQUEST, pattern = EmailApiFilter.PATTERN, methods = HttpConstants.METHOD_POST)
@ServiceDescription("Subra Mailer Email Api")
@ServiceRanking(-700)
@ServiceVendor("Subra")
public class EmailApiFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailApiFilter.class);

	protected static final String PATTERN = "/api/subra/mailer/v1/sendemail";

	@Reference
	TemplateService templateService;

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {
		final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
		EmailRequest emailRequest = SubraCommonHelper.convertToClass(RequestParser.getBody(slingRequest),
				EmailRequest.class);
		response.setContentType(SubraHttpType.MEDIA_TYPE_JSON.value());
		response.getWriter().write(SubraCommonHelper.writeValueAsString(templateService.sendEmail(emailRequest)));
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