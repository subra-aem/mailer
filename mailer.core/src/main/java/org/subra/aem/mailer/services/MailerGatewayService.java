package org.subra.aem.mailer.services;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * @author Raghava Joijode
 *
 *         MailerGatewayService connecting to SMTP to send emails
 */
public interface MailerGatewayService {

	boolean send(Email email) throws EmailException;

}
