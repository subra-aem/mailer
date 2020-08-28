package org.subra.aem.mailer.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subra.aem.flagapp.helpers.FlagAppHelper;
import org.subra.aem.mailer.services.MailerGatewayService;
import org.subra.aem.mailer.utils.EmailSenderType;

@Component(service = MailerGatewayService.class, immediate = true)
@ServiceDescription("Subra Mailer Gateway Service")
@Designate(ocd = MailerGatewayServiceImpl.Config.class, factory = true)
public final class MailerGatewayServiceImpl implements MailerGatewayService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailerGatewayServiceImpl.class);

	public static final int DEFAULT_SMTP_PORT = 465;
	public static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
	public static final String DEFAULT_SMTP_USER_NAME = "noreply.subra";
	public static final String DEFAULT_SMTP_FROM_ADDRESS = "noreply.subra@gmail.com";
	public static final String DEFAULT_SMTP_FROM_NAME = "Subra Technologies Group";
	public static final String DEFAULT_SMTP_USER_PASSWORD = "mrtfolqyvzzvmdzy";

	@ObjectClassDefinition(name = "Subra Mailer Gateway Service Configuration")
	public @interface Config {

		@AttributeDefinition(name = "SMTP Host", description = "Socket Timeout")
		String smtp_host() default DEFAULT_SMTP_HOST;

		@AttributeDefinition(name = "SMTP Port")
		int smtp_port() default DEFAULT_SMTP_PORT;

		@AttributeDefinition(name = "SMTP User")
		String smtp_user() default DEFAULT_SMTP_USER_NAME;

		@AttributeDefinition(name = "SMTP Password")
		String smtp_password() default DEFAULT_SMTP_USER_PASSWORD;

		@AttributeDefinition(name = "SMTP Enable SSL")
		boolean smtp_ssl() default true;

		@AttributeDefinition(name = "SMTP Enable Debug")
		boolean smtp_debug() default true;

		@AttributeDefinition(name = "Default Sender Email")
		String smtp_from_address() default DEFAULT_SMTP_FROM_ADDRESS;

		@AttributeDefinition(name = "Default Sender Name")
		String smtp_from_name() default DEFAULT_SMTP_FROM_NAME;

		@AttributeDefinition(name = "Connect With...", type = AttributeType.STRING)
		EmailSenderType connect_with() default EmailSenderType.NOREPLY;
	}

	@Activate
	protected void activate(final Config config) {
		setHost(config.smtp_host());
		setPort(config.smtp_port());
		setUser(config.smtp_user());
		setPassword(config.smtp_password());
		setSSL(config.smtp_ssl());
		setDebug(config.smtp_debug());
		setFromAddress(config.smtp_from_address());
		setFromName(config.smtp_from_name());
		LOGGER.trace("Email Service Connector Started");
	}

	private String host;

	private int port;

	private String user;

	private String password;

	private boolean isSSL;

	private boolean isDebug;

	private String fromAddress;

	private String fromName;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSSL() {
		return isSSL;
	}

	public void setSSL(boolean isSSL) {
		this.isSSL = isSSL;
	}

	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	private void createConnection(Email email) throws EmailException {
		email.setHostName(host);
		email.setSmtpPort(port);
		email.setAuthenticator(new DefaultAuthenticator(user, password));
		email.setSSLOnConnect(isSSL);
		if (email.getFromAddress() == null)
			email.setFrom(fromAddress, fromName);
		email.setDebug(isDebug);
		LOGGER.trace("Created Connection...with host \"{}\" for sending mail with subject \"{}\"", email.getHostName(),
				email.getSubject());
	}

	@Override
	public boolean send(Email email) throws EmailException {
		String messageId = null;
		createConnection(email);
		messageId = email.send();
		LOGGER.trace("Sent email, transaction id - {}", messageId);
		return StringUtils.isNotBlank(messageId);
	}

}
