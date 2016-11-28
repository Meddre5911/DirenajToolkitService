package direnaj.domain;

import java.io.Serializable;

public class ComparisonData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String campaign_id;
	private String hashtag;
	private Double sameUserPercentage;
	private Double sameUserCount;
	private Double totalComparedUserCount;
	private String requestId;

	public ComparisonData() {
	}

	public ComparisonData(String campaignId, String hashtag, Double sameUserPercentage, String requestId,
			Double sameUserCount, Double totalComparedUserCount) {
		this.setCampaign_id(campaignId);
		this.hashtag = hashtag;
		this.sameUserPercentage = sameUserPercentage;
		this.requestId = requestId;
		this.sameUserCount = sameUserCount;
		this.setTotalComparedUserCount(totalComparedUserCount);
	}

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public double getSameUserPercentage() {
		return sameUserPercentage;
	}

	public void setSameUserPercentage(double sameUserPercentage) {
		this.sameUserPercentage = sameUserPercentage;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getCampaign_id() {
		return campaign_id;
	}

	public void setCampaign_id(String campaign_id) {
		this.campaign_id = campaign_id;
	}

	public Double getSameUserCount() {
		return sameUserCount;
	}

	public void setSameUserCount(Double sameUserCount) {
		this.sameUserCount = sameUserCount;
	}

	public Double getTotalComparedUserCount() {
		return totalComparedUserCount;
	}

	public void setTotalComparedUserCount(Double totalComparedUserCount) {
		this.totalComparedUserCount = totalComparedUserCount;
	}

}