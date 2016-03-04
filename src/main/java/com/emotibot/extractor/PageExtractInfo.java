/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.emotibot.util.Tool;

public class PageExtractInfo {
	private String name="";
	private String firstPara="";
	private HashMap<String,String> attr = new HashMap<>();
	private List<Sentence> sentList = new ArrayList<>();
	public HashMap<String,String> getAttr() {
		return attr;
	}
	public void setAttr(HashMap<String,String> attr) {
		this.attr = attr;
	}
	public List<Sentence> getSentList() {
		return sentList;
	}
	public void setSentList(List<Sentence> sentList) {
		this.sentList = sentList;
	}
	
	public void addAttr(String key,String value)
	{
		if(Tool.isStrEmptyOrNull(key)) return ;
		attr.put(key, value);
		return ;
	}
	
	public void addSentList(Sentence sentStr)
	{
		if(sentStr!=null) sentList.add(sentStr);
        return ;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("name="+name).append("\r\n");
		buffer.append("Basic_Info:").append("\r\n");
		for(String key:attr.keySet())
		{
			buffer.append("attr="+key).append("\tvalue="+attr.get(key)).append("\r\n");
		}
		buffer.append("Sentence_Info:").append("\r\n");
		for(Sentence sent:sentList)
		{
			buffer.append(sent.toString()).append("======\r\n");
		}
		buffer.append("firstPara="+firstPara).append("======\r\n");
		return buffer.toString();
	}
	public String getFirstPara() {
		return firstPara;
	}
	public void setFirstPara(String firstPara) {
		this.firstPara = firstPara;
	}
	
	

}
