package org.subra.aem.mailer.internal.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.subra.aem.commons.constants.SubraHttpType;
import org.subra.aem.commons.helpers.RequestParser;
import org.subra.aem.commons.helpers.SubraCommonHelper;
import org.subra.aem.mailer.EmailRequest;
import org.subra.aem.mailer.services.TemplateService;

@Component(service = Servlet.class, property = { Constants.SERVICE_DESCRIPTION + "=Template Demo Servlet",
		"sling.servlet.methods=" + HttpConstants.METHOD_GET, "sling.servlet.paths=" + "/bin/subra/sendemail" })
public class SendEmailServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = -7639144471855594171L;

	@Reference
	TemplateService templateService;

	@Override
	protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		EmailRequest email = RequestParser.getBody(request, EmailRequest.class);
		Map<String, Object> result = templateService.sendEmail(email);
		response.setContentType(SubraHttpType.MEDIA_TYPE_JSON.value());
		response.getWriter().write(SubraCommonHelper.writeValueAsString(result));
	}

}
