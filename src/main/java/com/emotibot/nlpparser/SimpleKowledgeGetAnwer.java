package com.emotibot.nlpparser;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

public class SimpleKowledgeGetAnwer implements KowledgeGetAnswer {

	@Override
	public String getAnswer(String sentence) {
		// TODO Auto-generated method stub
		String  answer = AnalysisSentence.analysisSentenceToGetAnswer(sentence);
		return answer;
	}
	private String answerRewrite(String answer){
		String rewrite = "";
		
		return rewrite;
	}
   public static void main(String[] args){
	   
   }
}
