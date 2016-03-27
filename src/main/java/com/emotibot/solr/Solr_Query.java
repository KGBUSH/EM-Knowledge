package com.emotibot.solr;

import java.util.ArrayList;
import java.util.List;

public class Solr_Query {
	private boolean FindEntity=false;
	private String entity="";
	private List<String> words= new ArrayList<String>();
	public boolean isFindEntity() {
		return FindEntity;
	}
	public void setFindEntity(boolean findEntity) {
		FindEntity = findEntity;
	}
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public List<String> getWords() {
		return words;
	}
	public void setWords(List<String> words) {
		this.words = words;
	}
	public void addWord(String word)
	{
		if(word==null||word.trim().length()==0) return ;
		if(words==null) words= new ArrayList<String>();
		if(!words.contains(word)) words.add(word);
		return ;
 	}
	
	//public 
	public String getQuery()
	{
		StringBuffer buffer = new StringBuffer();
		if(FindEntity&&entity.trim().length()>0)
		{
			buffer.append("KG_Name:").append(entity);
			if(words.size()>0) buffer.append(" OR ");
			else buffer.append(" ");
		}
		for(int i=0;i<=words.size()-2;i++)
		{
			buffer.append("KG_Attr_Value:").append("*").append(words.get(i)).append("* OR ");
		}
		buffer.append("KG_Attr_Value:").append("*").append(words.get(words.size()-1)).append("*");
       System.err.println("query="+buffer.toString());
		return buffer.toString();
	}
}
