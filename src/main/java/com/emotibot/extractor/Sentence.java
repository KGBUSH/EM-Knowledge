/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.extractor;

import java.util.HashMap;
import java.util.Map;

public class Sentence {
	private String Sent="";
	private Map<String,String> MaoText_Url = new HashMap<>();
	public String getSent() {
		return Sent;
	}
	public void setSent(String sent) {
		Sent = sent;
	}
	public Map<String,String> getMaoText_Url() {
		return MaoText_Url;
	}
	public void setMaoText_Url(Map<String,String> maoText_Url) {
		MaoText_Url = maoText_Url;
	} 
	/*Baike sentence  MaoText_Url info*/
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.Sent).append("\r\n");
		for(String MaoText:MaoText_Url.keySet())
		{
			buffer.append(MaoText).append("  ").append(MaoText_Url.get(MaoText)).append("\r\n");
		}
		return buffer.toString();
	}

}
