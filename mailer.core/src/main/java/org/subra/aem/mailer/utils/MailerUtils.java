package org.subra.aem.mailer.utils;

public class MailerUtils {

	public static final String SENDER_EMAIL_ADDRESS = "senderEmailAddress";
	public static final String TO = "to";
	public static final String CC = "cc";
	public static final String BCC = "bcc";
	public static final String SENDER_NAME = "senderName";
	public static final String SUBJECT = "subject";
	public static final String BOUNCE_ADDRESS = "bounceAddress";
	public static final int DEFAULT_CONNECT_TIMEOUT = 30000;
	public static final int DEFAULT_SOCKET_TIMEOUT = 30000;

	public static final String DEFAULT_DRAFT_TEMPLATES_ID_PREFIX = "DR_";
	public static final String DEFAULT_APPROVED_TEMPLATES_ID_PREFIX = "AP_";

	public static final String DEFAULT_APPROVED_TEMPLATES_PATH = "/conf/foundation/settings/mailer/templates/subra";
	public static final String DEFAULT_DRAFT_TEMPLATES_PATH = "/var/subra/mailer/templates/draft";

	private MailerUtils() {
		throw new UnsupportedOperationException();
	}

}
