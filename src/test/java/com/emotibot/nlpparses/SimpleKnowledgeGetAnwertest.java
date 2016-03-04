package com.emotibot.nlpparses;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */

import com.emotibot.nlpparser.SimpleKnowledgeGetAnwer;


public class SimpleKnowledgeGetAnwertest {
	
	 public static void main(String[] args){
		   String sentence = "姚明是谁";
		   SimpleKnowledgeGetAnwer simpleKnowledgeGetAnwer = new SimpleKnowledgeGetAnwer();
		   System.out.println(simpleKnowledgeGetAnwer.getAnswer(sentence));
	   }
}
