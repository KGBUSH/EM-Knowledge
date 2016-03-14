package com.emotibot.nlpparses;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.emotibot.nlpparser.AnalysisSentence;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */

import com.emotibot.nlpparser.SimpleKnowledgeGetAnwer;


public class SimpleKnowledgeGetAnwertest {
   public static	 SimpleKnowledgeGetAnwer simpleKnowledgeGetAnwer = new SimpleKnowledgeGetAnwer();
	public static void testFetchAnswer() throws Exception {
		String testFile = "txt/test";

		final BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));
		String line2 = null;
		while ((line2 = reader2.readLine()) != null) {
			if (!line2.isEmpty()) {
			   System.out.println(line2);
			  
			   System.out.println(simpleKnowledgeGetAnwer.getAnswer(line2));
			}

		}

		reader2.close();
	}
	 public static void main(String[] args){
		 try {
			SimpleKnowledgeGetAnwertest.testFetchAnswer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   /*String sentence = "姚明的妻子是什么？";
		   SimpleKnowledgeGetAnwer simpleKnowledgeGetAnwer = new SimpleKnowledgeGetAnwer();
		   System.out.println("answer: "+simpleKnowledgeGetAnwer.getAnswer(sentence));*/
	   }
}
