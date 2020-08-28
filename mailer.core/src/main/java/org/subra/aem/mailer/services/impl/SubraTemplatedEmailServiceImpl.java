package org.subra.aem.mailer.services.impl;

import java.util.HashMap;
import java.util.Map;

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
import org.subra.aem.commons.utils.SubraStringUtils;
import org.subra.aem.mailer.services.MailerGatewayService;
import org.subra.aem.mailer.services.MailerService;
import org.subra.aem.mailer.services.SubraTemplatedEmailService;
import org.subra.aem.mailer.utils.EmailType;
import org.subra.aem.mailer.utils.MailerUtils;

@Component(service = SubraTemplatedEmailService.class, immediate = true)
@ServiceRanking(60000)
@ServiceDescription("Subra Templated Email Service")
@Designate(ocd = SubraTemplatedEmailServiceImpl.Config.class)
public final class SubraTemplatedEmailServiceImpl implements SubraTemplatedEmailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubraTemplatedEmailServiceImpl.class);

	@Reference
	private MailerService emailService;

	@Reference(target = "(connect.with=HELP)")
	private MailerGatewayService emailGatewayHelp;

	@Reference(target = "(connect.with=NOREPLY)")
	private MailerGatewayService emailGatewayNoReply;

	@Reference
	private MailerGatewayService emailGateway;

	public static final String DEFAULT_REGISTRATION_EMAIL_TEMPLATE = "/conf/foundation/settings/notification/email/subra/user-registration-email.txt";
	public static final String DEFAULT_INVITATION_EMAIL_TEMPLATE = "/conf/foundation/settings/notification/email/subra/registration-invitation-email.txt";
	public static final String DEFAULT_WELCOME_EMAIL_TEMPLATE = "/conf/foundation/settings/notification/email/subra/welcome-email.txt";
	public static final String DEFAULT_EXCEPTION_EMAIL_TEMPLATE = "/conf/foundation/settings/notification/email/subra/exception-email.txt";
	public static final String DEFAULT_GENERIC_EMAIL_TEMPLATE = "/conf/foundation/settings/notification/email/subra/generic-email.txt";
	public static final String DEFAULT_HTML_EMAIL_TEMPLATE = "/conf/foundation/settings/notification/email/subra/sample.html";

	private String registrationEmailTemplate;
	private String invititionEmailTemplate;
	private String welcomeEmailTemplate;
	private String exceptionEmailTemplate;
	private String genericEmailTemplate;
	private String htmlEmailTemplate;

	@ObjectClassDefinition(name = "Subra Templated Email Service Configuration", description = "Subra - Email Service")
	public @interface Config {

		@AttributeDefinition(name = "Registration Email Template", description = "Registration Email Template")
		String registration_email_template() default DEFAULT_REGISTRATION_EMAIL_TEMPLATE;

		@AttributeDefinition(name = "Invitation Email Template", description = "Invitition Email Template")
		String invitation_email_template() default DEFAULT_INVITATION_EMAIL_TEMPLATE;

		@AttributeDefinition(name = "Welcome Email Template", description = "Welcome Email Template")
		String welcome_email_template() default DEFAULT_WELCOME_EMAIL_TEMPLATE;

		@AttributeDefinition(name = "Exception Email Template", description = "Exception Email Template")
		String exception_email_template() default DEFAULT_EXCEPTION_EMAIL_TEMPLATE;

		@AttributeDefinition(name = "Generic Email Template", description = "Exception Email Template")
		String generic_email_template() default DEFAULT_GENERIC_EMAIL_TEMPLATE;

		@AttributeDefinition(name = "HTML Email Template", description = "Exception Email Template")
		String sample_html_email() default DEFAULT_HTML_EMAIL_TEMPLATE;
	}

	@Activate
	protected void activate(final Config config) {
		registrationEmailTemplate = config.registration_email_template();
		invititionEmailTemplate = config.invitation_email_template();
		welcomeEmailTemplate = config.welcome_email_template();
		exceptionEmailTemplate = config.exception_email_template();
		genericEmailTemplate = config.generic_email_template();
		htmlEmailTemplate = config.sample_html_email();
		LOGGER.info(
				"SubraTemplatedEmailService activated with [registrationEmailTemplate : {}] , [invititionEmailTemplate : {}], [welcomeEmailTemplate : {}], [exceptionEmailTemplate : {}], [genericEmailTemplate : {}]",
				registrationEmailTemplate, invititionEmailTemplate, welcomeEmailTemplate, exceptionEmailTemplate,
				genericEmailTemplate);
	}

	@Override
	public boolean email(EmailType type, String subject, String recipientName, String senderName, String link,
			Map<String, String> optionalParams, String... recipient) {
		boolean response = false;
		Map<String, String> emailParams = new HashMap<>();
		emailParams.put(MailerUtils.SUBJECT, subject);
		emailParams.put("recipientName", recipientName);
		emailParams.put(MailerUtils.SENDER_NAME, senderName);
		emailParams.put("link", link);
		emailParams.putAll(optionalParams);
		if (SubraStringUtils.isNoneBlank(subject, recipientName) && recipient.length > 0) {
			switch (type) {
			case GENERIC:
				response = sendEmail(emailGatewayNoReply, htmlEmailTemplate, emailParams, recipient);
				break;
			case EXCEPTION:
				response = sendEmail(emailGateway, exceptionEmailTemplate, emailParams, recipient);
				break;
			case WELCOME:
				response = sendEmail(emailGateway, welcomeEmailTemplate, emailParams, recipient);
				break;
			case INVITITION:
				response = sendEmail(emailGateway, invititionEmailTemplate, emailParams, recipient);
				break;
			case REGESTRATION:
			default:
				response = sendEmail(emailGateway, registrationEmailTemplate, emailParams, recipient);
				break;
			}
		}
		return response;
	}

	private boolean sendEmail(MailerGatewayService conn, String template, Map<String, String> parameters,
			String... recipient) {
		Map<String, Object> response = emailService.sendEmail(conn, template, parameters, recipient);
		return response.isEmpty();
	}

}
