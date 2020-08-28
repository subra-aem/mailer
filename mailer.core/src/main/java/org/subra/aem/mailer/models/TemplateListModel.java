package org.subra.aem.mailer.models;

import java.util.List;

import org.subra.aem.mailer.Template;

/**
 * @author Raghava Joijode
 *
 */
public interface TemplateListModel {

	default List<Template> getTemplates() {
		throw new UnsupportedOperationException();
	}

}
