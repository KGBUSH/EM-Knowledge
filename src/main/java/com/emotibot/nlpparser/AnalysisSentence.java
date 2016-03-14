package com.emotibot.nlpparser;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */
import java.util.ArrayList;
import java.util.List;

import com.emotibot.patternmatching.NLPProcess;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;

public class AnalysisSentence {
	
	public static SentenceTemplate sentenceTypeClassifier = new SentenceTemplate();
	
	/**
	 * get sentence attribute
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
public static void templateGet(List<Name_Type> entity,List<Name_Type> attribute,String sentence ){
	String templateAnswer = sentenceTypeClassifier.getSentenceType(sentence);
	if(templateAnswer.contains("##")){
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
	 * sentence to Struct
	 * 
	 * @param sentence
	 */
	public static String analysisSentenceToGetAnswer(String sentence) {
		String answer = "";
		List<Term> segpos = Pre_ProcessSentence.getSegPos(sentence);
		System.out.println(segpos);
		
		List<Name_Type> entity = new ArrayList<Name_Type>();
		List<Name_Type> attribute = new ArrayList<Name_Type>();
		templateGet(entity,attribute,sentence);
		/*System.out.println("template"+entity);
		System.out.println("template"+attribute);*/
		if(entity.size() == 0)
		entity = getEntity(segpos);
//		System.out.println(entity);
		if(attribute.size() == 0)
		{attribute = getAttribute(segpos);
//		System.out.println(attribute);
		/*for(int i = 0 ; i < attribute.size() ; i ++){
			if(entity.size()>=1){
			String curattri = NLPProcess.matchSynonymPropertyInDB(entity.get(0).value, attribute.get(i).value);
			if(!curattri.equals(attribute.get(i).value)){
				attribute.set(i, new Name_Type(curattri,"2"));
			}
			}
		}*/
	}
		answer = TraversalToGraph.traversal(entity, attribute);
		return answer;
	}

	public static void main(String[] args) {
		String sentence = "姚明的爱人是谁";
		AnalysisSentence.analysisSentenceToGetAnswer(sentence);

	}
}
