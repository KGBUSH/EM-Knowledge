package com.emotibot.understanding;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.log.LogService;
import com.emotibot.template.TemplateProcessor;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Tool;

public class QuestionClassifier {

	protected final static String introductionQuestionType = "IntroductionQuestion@:";
	protected final static String introductionSentenceType = "IntroductionSentence@:";
	// introductionRequestType is used to test an introduction after the
	// property recognize
	protected final static String introductionRequestType = "IntroductionRequest@:";
	protected final static String strictIntroductionQuestionType = "StrictIntroductionQuestion@:";
	protected final static String selectiveQuestionType = "SelectiveQuestion@:";
	protected final static String implicationQuestionType = "ImplicationQuestion@:";

	private final static TemplateProcessor questionClassifier = new TemplateProcessor("QuestionClassifier");

	// private NERBean nerBean = new NERBean();
	//
	// public QuestionClassifier(NERBean bean) {
	// nerBean = bean;
	// }

	// to test if it is a introduction request
	// input: 姚明是谁？ 你喜欢姚明吗？
	// output: 1 0
	protected static boolean isIntroductionRequest(String sentence, String entity) {
		boolean rs = false;
		if (Tool.isStrEmptyOrNull(sentence)) {
			return rs;
		}

		String template = getQuestionTemplateRS(sentence, entity);
		if (template.isEmpty()) {
			System.out.println("template=" + template + "~~~~ NOT introduction");
			rs = false;
		} else {
			if (template.startsWith(QuestionClassifier.introductionQuestionType)
					|| template.startsWith(QuestionClassifier.introductionSentenceType)) {
				rs = true;
				System.out.println(template + " IS " + QuestionClassifier.introductionQuestionType);
			} else {
				System.out.println("template=" + template + "~~~~ NOT introduction ?????");
				rs = false;
			}
		}

		return rs;
	}

	// to test if the user want to get the introduction of the entity
	// input: 姚明是谁？ 你喜欢姚明吗？
	// output: 1 0
	protected static boolean isKindofQuestion(String sentence, String questionType, String entity) {
		System.out.println("isKindofQuestion: questionType=" + questionType + ", entity=" + entity);
		boolean rs = false;
		if (Tool.isStrEmptyOrNull(sentence)) {
			return rs;
		}

		String template = "";
		template = getQuestionTemplateRS(sentence, entity);
		// if (!entity.isEmpty()) {
		// if (!sentence.contains(entity)) {
		// System.err.println("isKindofQuestion: sentence has no entity, s = " +
		// sentence + ", e=" + entity);
		// LogService.printLog("", "isKindofQuestion", "sentence has no entity,
		// s = " + sentence + ", e=" + entity);
		// return false;
		// }
		// String first = sentence.substring(0, sentence.indexOf(entity));
		// String second = sentence.substring(sentence.indexOf(entity) +
		// entity.length(), sentence.length());
		// System.out.println("isKindof: first=" + first + ", entity=" + entity
		// + ", second=" + second);
		// sentence = first + " ## " + entity + "<type>entity</type> " + second;
		// template = questionClassifier.process(sentence);
		// } else {
		// template = questionClassifier.processQuestionClassifier(sentence);
		// }

		if (!template.isEmpty() && template.startsWith(questionType)) {
			rs = true;
			System.out.println("~~~~ IS " + questionType);
		} else {
			System.out.println("template=" + template + "~~~~ NOT " + questionType);
		}
		return rs;
	}

	protected static String getQuestionTemplateRS(String sentence, String entity) {
		String template = "";
		if (!entity.isEmpty()) {
			if (!sentence.contains(entity)) {
				System.err.println("isKindofQuestion: sentence has no entity, s = " + sentence + ", e=" + entity);
				LogService.printLog("", "isKindofQuestion",
						"sentence has no entity, s = " + sentence + ", e=" + entity);
				return template;
			}
			String first = sentence.substring(0, sentence.indexOf(entity));
			String second = sentence.substring(sentence.indexOf(entity) + entity.length(), sentence.length());
			System.out.println("isKindof: first=" + first + ", entity=" + entity + ", second=" + second);
			String lastC = entity.charAt(entity.length() - 1) + "";
			if(CharUtil.isChinese(lastC)){
				sentence = first + " ## " + entity + "<type>entity</type> " + second;
			} else {
				sentence = first + " ## " + entity + " <type>entity</type> " + second;
			}
			template = questionClassifier.process(sentence);
		} else {
			template = questionClassifier.processQuestionClassifier(sentence);
		}

		return template;
	}

	// implication question process
	protected static AnswerBean implicationQuestionProcess(String sentence, String entity, AnswerBean answerBean) {
		String strImplication = questionClassifier.processQuestionClassifier(sentence).replace(implicationQuestionType,
				"");
		System.out.println("implicationQuestionProcess str = " + strImplication);

		if (!answerBean.getAnswer().isEmpty()) {
			String implicationAnswer = ImplicationProcess.getImplicationAnswer(answerBean.getAnswer(), entity, strImplication);
			if(!implicationAnswer.equals(answerBean.getAnswer())){
				answerBean.setAnswer(implicationAnswer);
				answerBean.setScore(500);
			}
		}
		System.out.println("Implication Qustion: anwerBean is " + answerBean.toString());
		return answerBean.returnAnswer(answerBean);
	}

	// to test if the user sentence is a question of relationship between two
	// entities
	protected static boolean isRelationshipQuestion(String sentence) {
		// TBD: hard code for 4/15
		if (sentence.contains("关系") || sentence.contains("联系"))
			return true;
		return false;
	}

	protected static AnswerBean selectiveQuestionProcess(String sentence, AnswerBean answerBean, NERBean nerBean) {
		String strSeletive = questionClassifier.processQuestionClassifier(sentence).replace(selectiveQuestionType, "");
		System.out.println("selectiveQuestionProcess str = " + strSeletive);

		if (!answerBean.getAnswer().isEmpty()) {
			// valide answer
			answerBean.setAnswer(strSeletive.substring(0, 1) + "的, " + nerBean.getEntitySet().get(0) + "的"
					+ answerBean.getProperty() + "是" + answerBean.getAnswer());
		} else {
			answerBean.setAnswer(strSeletive.substring(1));
		}
		System.out.println("Selective Qustion: anwerBean is " + answerBean.toString());
		return answerBean.returnAnswer(answerBean);
	}

}
