package com.emotibot.understanding;

import java.util.List;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.answerRewrite.AnswerRewrite;
import com.emotibot.common.Common;
import com.emotibot.util.Tool;

public class IntentionClassifier {

	private NERBean nerBean = new NERBean();

	public IntentionClassifier(NERBean bean) {
		nerBean = bean;
	}

	// The entrance to understand the user query and get answer from Neo4j
	// input: the question sentence from users,"姚明身高是多少"
	// output: the answer without answer rewriting, “226cm”
	public AnswerBean intentionProcess() {
		String sentence = nerBean.getSentence();
		List<String> entitySet = nerBean.getEntitySet();
		boolean isQuestion = nerBean.isQuestion();
		String uniqueID = nerBean.getUniqueID();

		AnswerBean answerBean = new AnswerBean();
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getAnswer: input is empty");
			return answerBean.returnAnswer(answerBean);
		}

		AnswerRewrite answerRewite = new AnswerRewrite();

		System.out.println("##### sentence"+sentence+", entitySet=" + entitySet);
		if (entitySet.size() == 1 && entitySet.get(0).equals(sentence)) {
			System.out.println("Single Entity Case: entity=" + entitySet.get(0));
			String tempEntity = entitySet.get(0);
			String tempLabel = DBProcess.getEntityLabel(tempEntity).toLowerCase();
			if (tempLabel.equals("catchword")) {
				System.out.println("catchword Case, and abord， the returned anwer is " + answerBean.toString());
				return answerBean;
			}

			if (NLPUtil.isInRemoveableAllDict(tempEntity) && NLPUtil.isInDomainBalckListDict(tempLabel)) {
				System.out.println("high frequent word in the blacklist domain case, and abord， the returned anwer is "
						+ answerBean.toString());
				return answerBean;
			}

			String tempStrIntroduce = DBProcess.getPropertyValue(entitySet.get(0),
					Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			if (tempStrIntroduce.contains("。"))
				tempStrIntroduce = tempStrIntroduce.substring(0, tempStrIntroduce.indexOf("。"));
			answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(tempStrIntroduce));
			answerBean.setScore(100);
			System.out.println("intentionProcess intro 1: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}

		// move the process of introduction question to intention process
		if (entitySet.size() == 1) {
			String tempEntity = entitySet.get(0);
			boolean isIntro = QuestionClassifier.isIntroductionRequest(NLPUtil.removePunctuateMark(sentence),
					isQuestion, tempEntity);
			if(isIntro){
				String strIntroduce = DBProcess.getPropertyValue(tempEntity, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
				if (strIntroduce.contains("。"))
					strIntroduce = strIntroduce.substring(0, strIntroduce.indexOf("。"));
				answerBean.setScore(100);
				answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(strIntroduce));
				System.out.println("intentionProcess intro 2: the returned anwer is " + answerBean.toString());
				return answerBean.returnAnswer(answerBean);
			}
		}

		return answerBean;
	}

}
