package com.emotibot.nlpparser;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */
import java.util.ArrayList;
import java.util.List;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;

public class AnalysisSentence {
	
	public static SentenceTemplate sentenceTypeClassifier = new SentenceTemplate();
	
	/**
	 * get sentence attribute by attribute Recognition
	 * 
	 * @param org
	 * @return
	 */
	public static List<Name_Type> getAttribute(List<Term> org) {
		List<Name_Type> attribute = new ArrayList<Name_Type>();
		for (int i = 0; i < org.size(); i++) {
			String poString = org.get(i).nature.toString();

			if (poString.equals("ude2") && !org.get(i).word.equals("地")) {
				String type = "2";
				Name_Type nType = new Name_Type(org.get(i).word, type);
				attribute.add(nType);
			}
		}
		return attribute;
	}
/**
 * get sentence Entity by Entity Recognition
 * @param org
 * @return
 */
	public static List<Name_Type> getEntity(List<Term> org) {
		List<Name_Type> entity = new ArrayList<Name_Type>();
		for (int i = 0; i < org.size(); i++) {
			String poString = org.get(i).nature.toString();

			if (poString.equals("nr")) {
				String type = "1";
				Name_Type nType = new Name_Type(org.get(i).word, type);
				entity.add(nType);
			}
		}
		return entity;
	}
	/**
	 * get sentence entity and attribute by template
	 * @param entity
	 * @param attribute
	 * @param sentence
	 */
public static void templateGet(List<Name_Type> entity,List<Name_Type> attribute,String sentence ){
	String templateAnswer = sentenceTypeClassifier.getSentenceType(sentence);
	if(templateAnswer.contains("##")){
	// splite entity and attribute by ##
	String [] array = templateAnswer.split("##");
	for(int i = 0 ; i  < array.length ; i ++){
		String wordpos = array[i];
		if(wordpos.contains("/")){
			String []wordposarr = wordpos.split("/");
			if(wordposarr.length>=2){
				String word = wordposarr[0];
				String pos =wordposarr[1];
				if(pos.equals("nr")){
					entity.add(new Name_Type(word, "1"));
				}
			 if(pos.equals("edu2 ")){
					attribute.add(new Name_Type(word, "2"));
				}
			}
		}
	}
	}
	
}
	
	/**
	 * anslysis sentence to get entity and attribute to traversal knowledgegraph get answer
	 * 
	 * @param sentence
	 */
	public static String analysisSentenceToGetAnswer(String sentence) {
		String answer = "";
		List<Term> segpos = Pre_ProcessSentence.getSegPos(sentence);
		System.out.println(segpos);
		
		List<Name_Type> entity = new ArrayList<Name_Type>();
		List<Name_Type> attribute = new ArrayList<Name_Type>();
		/**
		 * template to get entity and attribute
		 */
		templateGet(entity,attribute,sentence);
		
		/**
		 * if template get entity null then entity Recognition to get entity
		 */
		if(entity.size() == 0)
		entity = getEntity(segpos);

		/**
		 * if template get entity null then attribute Recognition to get attribute
		 */
		if(attribute.size() == 0)
		{
			attribute = getAttribute(segpos);

	    }
		/**
		 * traversal graph 
		 */
		if(entity.size() > 0 ){
		  
		  answer = TraversalToGraph.traversal(entity, attribute);
		}
		

		 return answer;
	}

	public static void main(String[] args) {
		String sentence = "姚明的爱人是谁";
		AnalysisSentence.analysisSentenceToGetAnswer(sentence);

	}
}
