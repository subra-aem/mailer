package org.subra.aem.mailer;

import java.util.Map;

import org.subra.aem.commons.helpers.SubraCommonHelper;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author Raghava Joijode
 * 
 *         Object to trigger an email, to pass all the required attributes
 */
public class EmailRequest {

	private String templateId;

	private Map<String, String> params;

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		try {
			return SubraCommonHelper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return super.toString();
		}
	}

}