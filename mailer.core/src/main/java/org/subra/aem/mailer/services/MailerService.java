package org.subra.aem.mailer.services;

import java.util.Map;

import javax.activation.DataSource;

import org.subra.aem.mailer.Template;

import com.drew.lang.annotations.NotNull;

/**
 * @author Raghava Joijode
 * 
 *         Mailer Service to Send Emails with either just template path or
 *         Template object.
 */
public interface MailerService {

	Map<String, Object> sendEmail(MailerGatewayService messageGateway, String templatePath,
			Map<String, String> emailParams, String... recipients);

	Map<String, Object> sendEmail(MailerGatewayService messageGateway, String templatePath,
			Map<String, String> emailParams, Map<String, DataSource> attachments, String... recipients);

	Map<String, Object> sendEmail(String templatePath, Map<String, String> emailParams);

	Map<String, Object> sendEmail(String templatePath, Map<String, String> emailParams, String... recipients);

	Map<String, Object> sendEmail(String templatePath, Map<String, String> emailParams,
			Map<String, DataSource> attachments, String... recipients);

	Map<String, Object> sendEmail(@NotNull Template template, Map<String, String> emailParams,
			MailerGatewayService messageGateway, Map<String, DataSource> attachments);

}
