package com.emotibot.solr;

import java.util.ArrayList;
import java.util.List;

public class Solr_Query {
	private boolean FindEntity=false;
	private List<String>  entity=new ArrayList<String>();;
	private List<String> words= new ArrayList<String>();
	public boolean isFindEntity() {
		return FindEntity;
	}
	public void setFindEntity(boolean findEntity) {
		FindEntity = findEntity;
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
		if(FindEntity&&entity.size()>0)
		{
			//buffer.append("KG_Name:").append(entity);
			for(int i=0;i<=entity.size()-2;i++)
			{
				buffer.append("KG_Name:").append("*").append(entity.get(i)).append("^2 ").append("* OR ");
			}
			buffer.append("KG_Name:").append("*").append(entity.get(entity.size()-1)).append("^2 ").append("*");
			if(words.size()>0) buffer.append(" OR ");
			else buffer.append(" ");
		}
		for(int i=0;i<=words.size()-2;i++)
		{
			buffer.append("KG_Attr_Value:").append("*").append(words.get(i)).append("^1 ").append("* OR ");
		}
		buffer.append("KG_Attr_Value:").append("*").append(words.get(words.size()-1)).append("^1 ").append("*");
       System.err.println("query="+buffer.toString());
		return buffer.toString();
	}
	public List<String> getEntity() {
		return entity;
	}
	public void setEntity(List<String> entity) {
		this.entity = entity;
	}
	public void addEntity(String ent) {
		if(ent==null||ent.trim().length()==0) return ;
		if(entity==null) entity= new ArrayList<String>();
		if(!entity.contains(ent)) entity.add(ent);
		return ;
	}

}
