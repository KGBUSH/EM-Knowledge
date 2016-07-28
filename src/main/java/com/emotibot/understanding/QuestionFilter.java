package com.emotibot.understanding;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.util.Tool;

public class QuestionFilter {

	private NERBean nerBean;
	
	public QuestionFilter(NERBean bean){
		nerBean = bean;
	}
	
	public AnswerBean filterSentence(){
		Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "QuestionFilter >>>>>> enter into filterSentence() and uniqueId is "+ nerBean.getUniqueID());
		AnswerBean answerBean = new AnswerBean();
		
		String sentence = nerBean.getSentence();
		if(Tool.isStrEmptyOrNull(sentence)){
			System.err.println("PMP.getAnswer: input is empty");
			return answerBean.returnAnswer(answerBean);
		}else if(sentence.startsWith("不")){
			answerBean.setValid(true);
			return answerBean.returnAnswer(answerBean);
		}else if(sentence.startsWith("你会")){
			answerBean.setValid(true);
			return answerBean.returnAnswer(answerBean);
		}else if(sentence.length() > 25 &&!sentence.contains("Rewrite:")) {
			answerBean.setValid(true);
			return answerBean.returnAnswer(answerBean);
		}
		Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "QuestionFilter >>>>>> return filterSentence() and uniqueId is " + nerBean.getUniqueID());
		return answerBean;
	}
	
}
