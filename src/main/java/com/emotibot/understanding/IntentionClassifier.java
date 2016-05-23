package com.emotibot.understanding;

import java.util.List;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.answerRewrite.AnswerRewrite;
import com.emotibot.common.Common;
import com.emotibot.template.TemplateEntry;
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

		System.out.println("##### sentence" + sentence + ", entitySet=" + entitySet);
		if (entitySet.size() == 1 && entitySet.get(0).equals(sentence)) {
			System.out.println("Single Entity Case: entity=" + entitySet.get(0));
			String tempEntity = entitySet.get(0);

			System.out.println("INTENTION 1");
			String tempLabel = DBProcess.getEntityLabel(tempEntity).toLowerCase();
			// if (tempLabel.equals("catchword")) {
			if (!NLPUtil.isInDomainWhiteListDict(tempLabel)) {
				System.out.println("catchword Case, and abord， the returned anwer is " + answerBean.toString());
				return answerBean;
			}

			// if (NLPUtil.isInRemoveableAllDict(tempEntity)) {
			if (NLPUtil.isInHighFrequentDict(tempEntity)) {
				System.out.println(
						"high frequent word not in the whitelist domain case, and abord， the returned anwer is "
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

		System.out.println("INTENTION 2, after Single Entity");

		// move the process of introduction question to intention process
		if (entitySet.size() == 1) {
			String tempEntity = entitySet.get(0);
			String tempLabel = DBProcess.getEntityLabel(tempEntity);
			String tempSentence = TemplateEntry.templateProcess(tempLabel, tempEntity, sentence, uniqueID);

			// print debug log
			if (Common.KG_DebugStatus || nerBean.isDebug()) {
				String tmpLabel = "";
				if (!entitySet.isEmpty()) {
					tmpLabel = DBProcess.getEntityLabel(entitySet.get(0));
				}
				String debugInfo = "DEBUG: userSentence=" + sentence + "; entitySet=" + entitySet + "; label="
						+ tmpLabel;
				debugInfo += "; template change to:" + tempSentence;
				answerBean.setComments(debugInfo);
				System.out.println(debugInfo);
				Debug.printDebug("123456", 1, "KG", debugInfo);
			}

			if (NLPUtil.isInRemoveableAllDict(tempEntity)) {
				System.out.println("high frequent word in the blacklist domain case, and abord， the returned anwer is "
						+ answerBean.toString());
				return answerBean;
			}

			boolean isIntro = QuestionClassifier.isIntroductionRequest(NLPUtil.removePunctuateMark(tempSentence),
					isQuestion, tempEntity);
			if (isIntro) {
				String strIntroduce = DBProcess.getPropertyValue(tempEntity, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
				if (strIntroduce.contains("。"))
					strIntroduce = strIntroduce.substring(0, strIntroduce.indexOf("。"));
				answerBean.setScore(100);
				answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(strIntroduce));
				System.out.println("intentionProcess intro 2: the returned anwer is " + answerBean.toString());
				return answerBean.returnAnswer(answerBean);
			}
		}
		System.out.println("INTENTION 3");

		return answerBean;
	}

}
