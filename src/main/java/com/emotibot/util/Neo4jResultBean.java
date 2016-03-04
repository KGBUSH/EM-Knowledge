package com.emotibot.util;

public class Neo4jResultBean {
	private boolean status;
	private String rs;
	
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getResult() {
		return rs;
	}
	public void setResult(String rs) {
		this.rs = rs;
	}

}
