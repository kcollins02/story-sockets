package com.jackson;

import com.jackson.objects.User;

public class Message {
    private String from;
    private String content;
    private String gameState;
    private User[] users;
    
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getGameState() {
		return gameState;
	}
	public void setGameState(String gameState) {
		this.gameState = gameState;
	}
	public void setUsers(User[] users) {
		this.users = users;
	}
	public User[] getUsers() {
		return users;
	}
}