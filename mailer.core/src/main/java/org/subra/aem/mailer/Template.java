package org.subra.aem.mailer;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.subra.aem.commons.helpers.SubraCommonHelper;
import org.subra.aem.commons.jcr.SubraResource;
import org.subra.aem.commons.jcr.utils.SubraResourceUtils;
import org.subra.aem.mailer.internal.helpers.MailerHelper;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author Raghava Joijode
 *
 *         Template object that implements SubraResource.
 */
public class Template extends SubraResource {

	private boolean isDraft = true;
	private String message;
	private List<String> lookUps;
	private String id;

	public Template(Resource resource) {
		super(resource);
		setMessage(SubraResourceUtils.getFileJCRData(getResource()));
		setId(MailerHelper.getTemplateId(this));
	}

	public boolean isDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	public List<String> getLookUps() {
		return lookUps;
	}

	public void setLookUps(List<String> lookUps) {
		this.lookUps = lookUps;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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