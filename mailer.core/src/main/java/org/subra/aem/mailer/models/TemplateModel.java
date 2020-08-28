package org.subra.aem.mailer.models;

import java.util.List;

import org.subra.aem.mailer.Template;

/**
 * @author Raghava Joijode
 *
 */
public interface TemplateModel {

	default Template getTemplate() {
		throw new UnsupportedOperationException();
	}

	default String getMessage() {
		throw new UnsupportedOperationException();
	}

	default List<String> getLookUpKeys() {
		throw new UnsupportedOperationException();
	}

}
