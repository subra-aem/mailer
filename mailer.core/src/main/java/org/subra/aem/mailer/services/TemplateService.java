package org.subra.aem.mailer.services;

import java.util.List;
import java.util.Map;

import org.subra.aem.commons.exceptions.SubraCustomException;
import org.subra.aem.mailer.EmailRequest;
import org.subra.aem.mailer.Template;

/**
 * @author Raghava Joijode
 *
 */
public interface TemplateService {

	List<Template> listTemplates();

	Template getTemplate(final String id) throws SubraCustomException;

	EmailRequest createOrUpdateTemplate(final String fileTitle, final String content);

	String readTemplate(final Template template);

	boolean deleteTemplate(final Template template);

	List<String> getLookUpKeys(final Template template);

	EmailRequest generateRequestFormat(final Template template);

	String generateEmailMarkUp(final EmailRequest email);

	Map<String, Object> sendEmail(final EmailRequest email);

}
