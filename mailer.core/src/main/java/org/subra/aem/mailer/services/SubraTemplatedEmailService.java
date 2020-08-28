package org.subra.aem.mailer.services;

import java.util.Map;

import org.subra.aem.mailer.utils.EmailType;

/**
 * @author Raghava Joijode
 *
 */
public interface SubraTemplatedEmailService {

	boolean email(EmailType type, String subject, String recipientName, String senderName, String link,
			Map<String, String> optionalParams, String... recipient);

}
