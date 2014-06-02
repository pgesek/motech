package org.motechproject.hub.mds;

// Generated Apr 21, 2014 6:59:44 PM by Hibernate Tools 3.4.0.CR1


import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;

/**
 * HubSubscriberTransaction generated by hbm2java
 */
@Entity
public class HubSubscriberTransaction implements java.io.Serializable {

	private static final long serialVersionUID = -2823908898058704053L;
	
	//TODO this should be an int ..not supported by mds
	@Field(required=true)
	private String hubDistributionStatusId;
	
	//TODO this should be an int ..not supported by mds
	@Field(required=true)
	private String hubSubscriptionId;
	
	//TODO this should be an int ..not supported by mds
	@Field(required=true)
	private String retryCount;
	
	@Field
	private String content;
	
	public String getHubDistributionStatusId() {
		return hubDistributionStatusId;
	}

	public void setHubDistributionStatusId(String hubDistributionStatusId) {
		this.hubDistributionStatusId = hubDistributionStatusId;
	}

	public String getHubSubscriptionId() {
		return hubSubscriptionId;
	}

	public void setHubSubscriptionId(String hubSubscriptionId) {
		this.hubSubscriptionId = hubSubscriptionId;
	}

	public String getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(String retryCount) {
		this.retryCount = retryCount;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Field(required=true)
	private String contentType;

	public HubSubscriberTransaction() {
	}

	public HubSubscriberTransaction(
			String hubDistributionStatusId,
			String hubSubscriptionId, String retryCount, String contentType) {
		this.hubDistributionStatusId = hubDistributionStatusId;
		this.hubSubscriptionId = hubSubscriptionId;
		this.retryCount = retryCount;
		this.contentType = contentType;
	}

	public HubSubscriberTransaction(String hubDistributionStatusId,
			String hubSubscriptionId, String retryCount, String contentType,
			String content) {
		this.hubDistributionStatusId = hubDistributionStatusId;
		this.hubSubscriptionId = hubSubscriptionId;
		this.retryCount = retryCount;
		this.contentType = contentType;
		this.content = content;
	}

	

}