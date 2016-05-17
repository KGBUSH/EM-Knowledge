package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.List;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.answerRewrite.AnswerRewrite;
import com.emotibot.common.Common;
import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.template.TemplateEntry;
import com.emotibot.util.CUBean;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class KGAgent {

	private String userSentence;
	private List<Term> segPos;
	private List<String> segWordWithoutStopWord;
	private List<String> entitySet;
	private String uniqueID = "";
	
	
	private boolean isQuestion = false;
	private long timeCounter = System.currentTimeMillis();
	
	
	private NERBean nerBean = new NERBean();
	
	protected boolean debugFlag = false;
	

	public KGAgent(CUBean cuBean) {
		String text = cuBean.getText();
		String questionType = cuBean.getQuestionType();
		String requestScore = cuBean.getScore();
		double questionScore = 0;
		uniqueID = cuBean.getUniqueID();
		if (Tool.isStrEmptyOrNull(uniqueID)) {
			uniqueID = "0";
		}

		// add for debug by PM
		if (questionType != null && questionType.equals("debug")) {
			System.out.println("DEBUG is TRUE");
			debugFlag = true;
		} else {
			debugFlag = false;
		}
		// System.err.println("questionType="+questionType+", debugFlag =
		// "+debugFlag);

		Debug.printDebug(uniqueID, 3, "knowledge", "init of PatternMatchingProcess:" + cuBean.toString());

		if (text == null) {
			System.err.println("text is null");
			Debug.printDebug(uniqueID, 2, "knowledge", "init, text is null");
			text = "";
		}
		
		nerBean.setOldSentence(CharUtil.trimAndlower(text));
		nerBean.setUniqueID(uniqueID);
		
		userSentence = text.toLowerCase();

		if (questionType == null || requestScore == null) {
			Debug.printDebug(uniqueID, 2, "knowledge", "init, question or score is null");
			System.err.println("question or score is null");
			questionType = "";
			requestScore = "";
		} else if (questionType.equals("question")) {
			try {
				questionScore = Double.parseDouble(requestScore);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (userSentence.endsWith("?") || userSentence.endsWith("？")) {
			isQuestion = true;
			nerBean.setQuestion(true);
		} else if (questionScore >= 0.3) {
			isQuestion = true;
			nerBean.setQuestion(true);
		}
		userSentence = NLPUtil.removePunctuateMark(userSentence);
		nerBean.setSentence(userSentence);

		System.out.println("userSentence=" + userSentence + ", isQuestion=" + isQuestion);
		segPos = NLPUtil.getSegWord(userSentence);
		System.out.println("Constructor: segPos=" + segPos);
		segWordWithoutStopWord = new ArrayList<>();
		for (int i = 0; i < segPos.size(); i++) {
			String segWord = CharUtil.trim(segPos.get(i).word);
			System.out.println("segWord=" + segWord);
			if (!NLPUtil.isStopWord(segWord)) {
				segWordWithoutStopWord.add(segWord);
			}
		}
		
		nerBean.setSegPos(segPos);
		nerBean.setSegWordWithoutStopWord(segWordWithoutStopWord);
		
		System.out.println("TIME 3 - before get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
		// change the follow method of getting entity, by the case:
		// "玛丽和马克思的其他中文名叫什么"
		// note:"和" is stop word
		// entitySet = getEntity(NLPProcess.removeStopWord(userSentence));
		
		EntityRecognizer entityActor = new EntityRecognizer(nerBean);
		nerBean = entityActor.updateNERBean();
		entitySet = nerBean.getEntitySet();
		userSentence = nerBean.getSentence();
		System.out.println("TIME 4 - get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
		System.out.println("KGAgent: bean=" + nerBean.toString());
	}
	
	public AnswerBean getAnswer() {
		
		AnswerBean answerBean = new AnswerBean();
		IntentionClassifier intention = new IntentionClassifier(nerBean);
		answerBean = intention.intentionProcess();
		if(!answerBean.isValid()){
			answerBean = answerProcess();
		}
		
		return answerBean;
	}
	
	// The entrance to understand the user query and get answer from Neo4j
	// input: the question sentence from users,"姚明身高是多少"
	// output: the answer without answer rewriting, “226cm”
	private AnswerBean answerProcess() {

		String sentence = nerBean.getSentence();
		AnswerBean answerBean = new AnswerBean();
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getAnswer: input is empty");
			return answerBean.returnAnswer(answerBean);
		}

		AnswerRewrite answerRewite = new AnswerRewrite();

		System.out.println("##### entitySet="+entitySet);
//		if (entitySet.size() == 1 && entitySet.get(0).equals(sentence)) {
//			System.out.println("Single Entity Case: entity=" + entitySet.get(0));
//			String tempEntity = entitySet.get(0);
//			String tempLabel = DBProcess.getEntityLabel(tempEntity).toLowerCase();
//			if(tempLabel.equals("catchword")){
//				System.out.println("catchword Case, and abord， the returned anwer is " + answerBean.toString());
//				return answerBean;
//			}
//			
//			if(NLPUtil.isInRemoveableAllDict(tempEntity) && NLPUtil.isInDomainBalckListDict(tempLabel)){
//				System.out.println("high frequent word in the blacklist domain case, and abord， the returned anwer is " + answerBean.toString());
//				return answerBean;
//			}
//			
//			String tempStrIntroduce = DBProcess.getPropertyValue(entitySet.get(0), Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
//			if (tempStrIntroduce.contains("。"))
//				tempStrIntroduce = tempStrIntroduce.substring(0, tempStrIntroduce.indexOf("。"));
//			answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(tempStrIntroduce));
//			answerBean.setScore(100);
//			System.out.println("PM.getAnswer 0.1: the returned anwer is " + answerBean.toString());
//			return answerBean.returnAnswer(answerBean);
//		}

		if (isQuestion == false) {
			Debug.printDebug(uniqueID, 4, "knowledge", "the input sentence is not a question");
			return answerBean.returnAnswer(answerBean);
		}

//		System.out.println(x);
		if (Common.KG_DebugStatus || debugFlag) {
			String tempLabel = "";
			if (!entitySet.isEmpty()) {
				tempLabel = DBProcess.getEntityLabel(entitySet.get(0));
			}
			String debugInfo = "DEBUG: userSentence=" + userSentence + "; entitySet=" + entitySet + "; label=" + tempLabel;
			System.out.println(debugInfo);
			
			Debug.printDebug("123456", 1, "KG", debugInfo);
			answerBean.setComments(debugInfo);
		}

		// 1. get the entity and Revise by template
//		if (entitySet.size() > 2) {
//			// check for 4/15 temporarily, may extent later
//			System.err.println("NOTES: PM.getAnswer: there are more than two entities");
//		}

		// TBD: if the sentence does not contain the entity, go through the
		// process of matching value of properties
		System.out.println("PMP.getAnswer: entity = " + entitySet.toString());
		if (entitySet == null || entitySet.isEmpty()) {
			System.out.println("the sentence does not contain entity name and so return empty");
			return answerBean.returnAnswer(answerBean);
		}

		// PatternMatchingResultBean beanPM = new PatternMatchingResultBean();
		String entity = "";
		// for single entity case
		// if (entitySet.size() == 1) {
		System.out.println("TEMP 1 answerBean="+answerBean.toString());
		if (entitySet.size() == 1) {
			entity = entitySet.get(0);
			System.out.println("TIME 5 - get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));

			// iterate each label of entity, and get the answer with highest
			// score
			List<String> listLabel = DBProcess.getEntityLabelList(entity);
			String oldSentence = sentence;
			List<AnswerBean> singleEntityAnswerBeanList = new ArrayList<>();

			for (String iLabel : listLabel) {
				String tempSentence = TemplateEntry.templateProcess(iLabel, entity, sentence, uniqueID);
				AnswerBean tempBean = new AnswerBean();

				// print debug log
				if (Common.KG_DebugStatus || debugFlag) {
					String debugInfo = answerBean.getComments()+"\n template process: ilabel:" + iLabel + " from:" + oldSentence + " to:"
							+ tempSentence;
					System.out.println(debugInfo);
					Debug.printDebug("123456", 1, "KG", debugInfo);
					tempBean.setComments(debugInfo);
				}

				if (Tool.isStrEmptyOrNull(tempSentence)) {
					continue;
				}
				PropertyRecognizer propertyRecognizer = new PropertyRecognizer(nerBean);
				tempBean = propertyRecognizer.ReasoningProcess(tempSentence, iLabel, entity, tempBean);
				System.out.println("\t ReasoningProcess answerBean = " + tempBean);

				// add the implicationQuestion process here, for now only check
				// the year computing
				if (QuestionClassifier.isKindofQuestion(userSentence, QuestionClassifier.implicationQuestionType, "")) {
					tempBean = QuestionClassifier.implicationQuestionProcess(userSentence, entity, tempBean);
					// answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer(),
					// 0));
					System.out.println("Implication Qustion: tempBean is " + tempBean.toString());
				}

				if (tempBean.isValid()) {
					singleEntityAnswerBeanList.add(tempBean);
				}
				
				System.out.println("TEMP answerBean="+tempBean);
			}

			if (!singleEntityAnswerBeanList.isEmpty()) {
				System.out.println("singleEntityAnswerBeanList: size = " + singleEntityAnswerBeanList.size());
				AnswerBean tempBean = singleEntityAnswerBeanList.get(0);
				System.out.println("first = " + tempBean);
				for (int i = 1; i < singleEntityAnswerBeanList.size(); i++) {
					System.out.println(i + "th = " + singleEntityAnswerBeanList.get(i));
					if (tempBean.getScore() < singleEntityAnswerBeanList.get(i).getScore()) {
						tempBean = singleEntityAnswerBeanList.get(i);
					}
				}
				answerBean = tempBean;
			}

			System.out.println("\t Single Entity answerBean = " + answerBean);
			System.out.println("TIME 6 - Single Entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));

		} else if (QuestionClassifier.isRelationshipQuestion(userSentence)) {
			List<String> relationDirectNormalWayPathSet = DBProcess.getRelationshipTypeInStraightPath("",
					entitySet.get(0), "", entitySet.get(1), 1);
			System.out
					.println("TIME 8 - get relationships >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));

			String answerRelation = "";
			if (!relationDirectNormalWayPathSet.isEmpty()) {
				String directNormalWayRelation = entitySet.get(1) + "是" + entitySet.get(0);
				for (String s : relationDirectNormalWayPathSet) {
					directNormalWayRelation += "的" + s;
				}
				answerRelation = directNormalWayRelation;
				System.out.println("\t directNormalWayRelation = " + directNormalWayRelation);
			}

			if (answerRelation.isEmpty()) {
				List<String> relationDirectReverseWayPathSet = DBProcess.getRelationshipTypeInStraightPath("",
						entitySet.get(1), "", entitySet.get(0), 1);
				if (!relationDirectReverseWayPathSet.isEmpty()) {
					System.out.println("relationReverseWayPathSet=" + relationDirectReverseWayPathSet);
					String directReverseWayRelation = entitySet.get(0) + "是" + entitySet.get(1);
					for (String s : relationDirectReverseWayPathSet) {
						directReverseWayRelation += "的" + s;
					}
					answerRelation = directReverseWayRelation;
					System.out.println("\t directReverseWayRelation = " + directReverseWayRelation);
				}
			}

			if (answerRelation.isEmpty()) {
				List<String> relationNormalWayPathSet = DBProcess.getRelationshipTypeInStraightPath("",
						entitySet.get(0), "", entitySet.get(1), 2);
				if (!relationNormalWayPathSet.isEmpty()) {
					System.out.println("relationNormalWayPathSet=" + relationNormalWayPathSet);
					String normalWayRelation = entitySet.get(1) + "是" + entitySet.get(0);
					for (String s : relationNormalWayPathSet) {
						normalWayRelation += "的" + s;
					}
					answerRelation = normalWayRelation;
					System.out.println("\t normalWayRelation = " + normalWayRelation);
				}
			}

			if (answerRelation.isEmpty()) {
				List<String> relationReverseWayPathSet = DBProcess.getRelationshipTypeInStraightPath("",
						entitySet.get(1), "", entitySet.get(0), 2);
				if (!relationReverseWayPathSet.isEmpty()) {
					System.out.println("relationReverseWayPathSet=" + relationReverseWayPathSet);
					String reverseWayRelation = entitySet.get(0) + "是" + entitySet.get(1);
					for (String s : relationReverseWayPathSet) {
						reverseWayRelation += "的" + s;
					}
					answerRelation = reverseWayRelation;
					// answerRelation = (answerRelation.isEmpty()) ?
					// reverseWayRelation
					// : answerRelation + "；" + reverseWayRelation;
					System.out.println("\t reverseWayRelation = " + reverseWayRelation);
				}
			}

			if (answerRelation.isEmpty()) {
				List<List<String>> relationConvergePathSet = DBProcess.getRelationshipTypeInConvergePath("",
						entitySet.get(0), "", entitySet.get(1));
				if (!relationConvergePathSet.isEmpty()) {
					System.out.println("relationConvergePathSet=" + relationConvergePathSet);
					String convergeRelation = entitySet.get(0) + "和" + entitySet.get(1) + "的";
					for (List<String> listStr : relationConvergePathSet) {
						convergeRelation += listStr.get(1) + "都是" + listStr.get(0) + "，";
					}
					convergeRelation = convergeRelation.substring(0, convergeRelation.length() - 1);
					// for now, return the longest one
					convergeRelation = Tool.getLongestStringInArray(convergeRelation.split("，"));
					answerRelation = convergeRelation;
					// answerRelation = (answerRelation.isEmpty()) ?
					// convergeRelation
					// : answerRelation + "；" + convergeRelation;
					System.out.println("\t convergeRelation = " + convergeRelation);
				}
			}

			if (answerRelation.isEmpty()) {
				List<List<String>> relationDivergePathSet = DBProcess.getRelationshipTypeInDivergentPath("",
						entitySet.get(0), "", entitySet.get(1));
				if (!relationDivergePathSet.isEmpty()) {
					System.out.println("relationDivergePathSet=" + relationDivergePathSet);
					String divergeRelation = entitySet.get(0) + "和" + entitySet.get(1) + "都是";
					for (List<String> listStr : relationDivergePathSet) {
						divergeRelation += listStr.get(0) + "的" + listStr.get(1) + "，";
					}
					divergeRelation = divergeRelation.substring(0, divergeRelation.length() - 1);
					// for now, return the longest one
					divergeRelation = Tool.getLongestStringInArray(divergeRelation.split("，"));
					answerRelation = divergeRelation;
					// answerRelation = (answerRelation.isEmpty()) ?
					// divergeRelation
					// : answerRelation + "；" + divergeRelation;
					System.out.println("\t divergeRelation = " + divergeRelation);
				}
			}

			if (!answerRelation.isEmpty()) {
				answerBean.setAnswer(answerRewite.rewriteAnswer(answerRelation, 1));
				answerBean.setScore(100);
			}

			System.out.println("RETURN of GETANSWER: Relationship Qustion: anwerBean is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		} else {
			System.err.println(
					"there are more than a entity, but it is not a relationship question. entitySet = " + entitySet);
			return answerBean.returnAnswer(answerBean);
		}

		System.out
				.println("TIME 9 - before answer rewrite >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));

		System.out.println("\t into selective question, answerBean=" + answerBean);
		// if it is the selective question
		if (QuestionClassifier.isKindofQuestion(userSentence, QuestionClassifier.selectiveQuestionType, "")) {
			answerBean = QuestionClassifier.selectiveQuestionProcess(userSentence, answerBean, nerBean);
			answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer(), 2));
			System.out.println("RETURN of GETANSWER: Selective Qustion: anwerBean is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}

		if (!answerBean.getAnswer().isEmpty()) {
			// normal case
			if (isQuestion == false) {
				answerBean.setScore(0);
			}
			answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer()));
			System.out.println("PM.getAnswer 5: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		} else {
			// introduction case
			String localAnswer = "";
			if (!userSentence.contains(entity)) {
				System.out.println("userSentence=" + userSentence + "++++ entity=" + entity);
				localAnswer = CommonUtil.matchPropertyValue(entity, segWordWithoutStopWord).replace("----####", "是" + entity + "的")
						+ "。" + entity + "是";
				// System.out.println("segWordWithoutStopWord="+segWordWithoutStopWord+",
				// tempProp="+tempProp+", replacePro="+replaceProp);
			}
			String strIntroduce = DBProcess.getPropertyValue(entity, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			if (strIntroduce.contains("。"))
				strIntroduce = strIntroduce.substring(0, strIntroduce.indexOf("。"));
			localAnswer += strIntroduce;
			answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(localAnswer));
			answerBean.setScore(
					QuestionClassifier.isKindofQuestion(NLPUtil.removePunctuateMark(userSentence), "Introduction", entity)
							? 100 : 0);
			if (isQuestion == false) {
				answerBean.setScore(0);
			}
			System.out.println("PM.getAnswer 7: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}
	}

	public static void main(String[] args) {
//		NLPProcess nlpProcess = new NLPProcess();
//		NLPProcess.NLPProcessInit();
		DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
		DictionaryBuilder.DictionaryBuilderInit();
		String str = "一吻定情是哪个国家的？";
		CUBean bean = new CUBean();
		bean.setText(str);
//		bean.setQuestionType("question");
		bean.setQuestionType("debug");
		bean.setScore("50");
//		PatternMatchingProcess mp = new PatternMatchingProcess(bean);
//		AnswerBean bean1 = mp.getAnswer();
		
		KGAgent agent = new KGAgent(bean);
		AnswerBean bean2 = agent.getAnswer();
		
//		System.out.println("PM Method: "+ bean1);
		System.out.println("KG Method: "+ bean2);
		System.out.println("debug comments=" + bean2.getComments());

	}

}
