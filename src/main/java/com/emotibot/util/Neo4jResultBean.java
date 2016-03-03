package com.emotibot.util;

public class Neo4jResultBean {
	private boolean status;
	private String rs;
	private String exception;
	public Neo4jResultBean()
	{
		this.status=true;
		rs="";
		exception="";
	}
	
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
	
	public void setException(String ex)
	{
		exception=ex;
	}
	
   public String toString()
   {
	   StringBuffer buffer = new StringBuffer();
	   buffer.append("status="+status).append("\r\n");
	   buffer.append("rs="+rs).append("\r\n");
	   buffer.append("exception="+exception).append("\r\n");
	   return buffer.toString();

   }

}
