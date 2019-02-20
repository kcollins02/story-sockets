package com.jackson.objects;

public class User {
	private String username;
	private String vote;
	private Boolean isScrumMaster;
	

	public User (String username, Boolean isScrumMaster) {
		this.username = username;
		this.vote = "?";
		this.isScrumMaster = isScrumMaster;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getVote() {
		return vote;
	}
	public void setVote(String vote) {
		this.vote = vote;
	}
	public void setIsScrumMaster(Boolean isScrumMaster) {
		this.isScrumMaster = isScrumMaster;
	}
	public Boolean getIsScrumMaster() {
		return isScrumMaster;
	}
	
	public void onContent (String content) {
		if (!this.isScrumMaster) {
			this.vote = content;
		}
	}
	
}
