package com.emotibot.nlpparser;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */
public class SimpleKnowledgeGetAnwer {
	public static String getAnswer(String sentence) {
		// TODO Auto-generated method stub
		String  answer = AnalysisSentence.analysisSentenceToGetAnswer(sentence);
		
		return answer;
	}
	private String answerRewrite(String answer){
		String rewrite = "";
		
		return rewrite;
	}
  
}
