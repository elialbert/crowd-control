package com.elialbert.cc1;

public class Userkey {
	private String username;
	private Long key;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Userkey(String username, Long key) {
		this.username = username;
		this.key = key;
	}
}