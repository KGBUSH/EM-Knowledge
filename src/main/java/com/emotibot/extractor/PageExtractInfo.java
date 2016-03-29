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

import org.apache.commons.lang.StringEscapeUtils;

import com.emotibot.util.Tool;

public class PageExtractInfo {
	private String name="";
	private String firstPara="";
	private HashMap<String,String> attr = new HashMap<>();
	private List<Sentence> sentList = new ArrayList<>();
	private HashMap<String,List<String>> attr_Values = new HashMap<>();

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
		if(value==null) return ;
		if(key.contains("<")||key.contains(">")) return ;
		key=key.replaceAll("/", "");//
		key=key.replaceAll("\\.", "");
		key=key.replaceAll("!", "");
		key=key.replaceAll("\\?", "");
		key=key.replaceAll("\\*", "");
		//" “ ”
		key = key.replaceAll("[\\pP‘’“”]", "");
		key = key.replaceAll("[0-9]", "");

		key=key.replaceAll("\"", "");
		key=key.replaceAll("“", "");
		key=key.replaceAll("”", "");
        key=removeAllBlank(key);
		if(Tool.isStrEmptyOrNull(key)) return ;

		value=value.replace("'", " ");
		value=value.replace("\\", "");
		value=value.replace("/", "");
		value=value.replaceAll("\"", "");
		value=value.replaceAll("“", " ");
		value=value.replaceAll("”", " ");

		value=StringEscapeUtils.escapeSql(value);

		attr.put(key, value);
		return ;
	}
	public static String removeAllBlank(String s){  
	    String result = "";  
	    if(null!=s && !"".equals(s)){  
	        result = s.replaceAll("[　*| *| *|//s*]*", "");  
	    }  
	    return result;  
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
	//KG_Name
	//KG_Attr
	//KG_Value
	//KG_Attr_Value
	//KG_Info
	public String getAttrStr()
	{
		StringBuffer buffer = new StringBuffer();
		for(String key:attr.keySet())
		{
			buffer.append(key).append("\t");
		}
        return buffer.toString().trim();
	}
	public String getValueStr()
	{
		StringBuffer buffer = new StringBuffer();
		for(String key:attr.keySet())
		{
			buffer.append(attr.get(key)).append("\t");
		}
        return buffer.toString().trim();
	}
	public String getAttrValueStr()
	{
		StringBuffer buffer = new StringBuffer();
		for(String key:attr.keySet())
		{
			buffer.append(key).append("\t").append(attr.get(key)).append("\t");
		}
        return buffer.toString().trim();
	}
	public String toSolrString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(name).append("\t");
		for(String key:attr.keySet())
		{
			buffer.append(key).append("\t"+attr.get(key)).append("\t");
		}
		for(Sentence sent:sentList)
		{
			buffer.append(sent.toString()).append("\t");
		}
		buffer.append(firstPara).append("\t");
		return buffer.toString().trim();
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
		buffer.append("attr_Values=").append("======\r\n");
		for(String attr:attr_Values.keySet())
		{
			buffer.append("attr="+attr).append(" ").append(attr_Values.get(attr).toString()).append("\r\n");
		}

		return buffer.toString();
	}
	public String getFirstPara() {
		return firstPara;
	}
	public void setFirstPara(String firstPara) {
		this.firstPara = firstPara;
	}
	public HashMap<String,List<String>> getAttr_Values() {
		return attr_Values;
	}
	public void setAttr_Values(HashMap<String,List<String>> attr_Values) {
		this.attr_Values = attr_Values;
	}
	
	public void addAttr_Values(String attr,String value)
	{
		if(Tool.isStrEmptyOrNull(attr)) return ;
		if(Tool.isStrEmptyOrNull(value)) return ;
		if(attr.contains("<")||attr.contains(">")) return ;
		attr=attr.replaceAll("/", "");//
		attr=attr.replaceAll("\\.", "");
		attr=attr.replaceAll("!", "");
		attr=attr.replaceAll("\\?", "");
		attr=attr.replaceAll("\\*", "");
		//" “ ”
		attr = attr.replaceAll("[\\pP‘’“”]", "");
		attr = attr.replaceAll("[0-9]", "");

		attr=attr.replaceAll("\"", "");
		attr=attr.replaceAll("“", "");
		attr=attr.replaceAll("”", "");
		attr=removeAllBlank(attr);
		if(Tool.isStrEmptyOrNull(attr)) return ;
		//attr=attr.toLowerCase();
		value=value.replace("'", " ");
		value=value.replace("\\", "");
		value=value.replace("/", "");
		value=value.replaceAll("\"", "");
		value=value.replaceAll("“", " ");
		value=value.replaceAll("”", " ");

		value=StringEscapeUtils.escapeSql(value);

		if(attr_Values.containsKey(attr))
		{
			attr_Values.get(attr).add(value);
		}
		else
		{
			attr_Values.put(attr, new ArrayList<String>());
			attr_Values.get(attr).add(value);
		}
	}
	
	public static void main(String args[])
	{
		String key=".";
		key=key.replaceAll("/", "");//
		key=key.replaceAll("\\.", "");
		key=key.replaceAll("!", "");
		key=key.replaceAll("\\?", "");
		key=key.replaceAll("\\*", "");
		//" “ ”
		key = key.replaceAll("[\\pP‘’“”]", "");
		key = key.replaceAll("[0-9]", "");

		key=key.replaceAll("\"", "");
		key=key.replaceAll("“", "");
		key=key.replaceAll("”", "");
        key=removeAllBlank(key);
        System.err.println(key);
	}
	

}
