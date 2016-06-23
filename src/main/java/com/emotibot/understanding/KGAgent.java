package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			nerBean.setDebug(true);
			questionScore = 100;
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

//		nerBean.setOldSentence(CharUtil.trimAndlower(text));	// fix the bad case "你好。"
		
		nerBean.setUniqueID(uniqueID);

		userSentence = text.toLowerCase();

		if (questionType == null || requestScore == null) {
			Debug.printDebug(uniqueID, 2, "knowledge", "init, question or score is null");
			System.err.println("question or score is null");
			questionType = "";
			requestScore = "";
		} else if (questionType.startsWith("question")) {
			try {
				questionScore = Double.parseDouble(requestScore);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		nerBean.setQuestionScore(questionScore);

		// System.out.println("questionType="+questionType+",
		// questionScore="+questionScore);

		if (userSentence.endsWith("?") || userSentence.endsWith("？")) {
			isQuestion = true;
			nerBean.setQuestion(true);
		} else if (questionScore >= 20) {
			isQuestion = true;
			nerBean.setQuestion(true);
		}
		userSentence = NLPUtil.removePunctuateMark(userSentence);
		nerBean.setSentence(userSentence);
		nerBean.setOldSentence(userSentence);	// fix the bad case "你好。"
		
		if(userSentence.isEmpty()){
			return;
		}

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
		if (Tool.isStrEmptyOrNull(nerBean.getOldSentence()) || CharUtil.isNumberFormat(nerBean.getOldSentence())) {
			System.err.println("PMP.getAnswer: input is empty or is in wrong format");
			return answerBean.returnAnswer(answerBean);
		}

		// Preprocess: removing the senetence begin with words like "你喜欢"
		
		
		
		// Intention Process
		IntentionClassifier intention = new IntentionClassifier(nerBean);
		answerBean = intention.intentionProcess();

		if (answerBean.isValid()) {
			// if answerBean is valid, which means intention generates it, return this answer
			System.out.println("bean retuned by intention = " + answerBean);
			return answerBean;
		} else {
			answerBean = answerProcess(answerBean);
		}
		
		double score = answerBean.getScore();
		System.out.println("pre score==="+score);
		// if it is not a question, then lower the score of the answer
		// since if there is another answer from other module, the answer from KG with lower score will not be selected
		if(nerBean.getQuestionScore() < 5){
			score = 0;
		}
		
		score = (score > 100) ? 100 : score;
		answerBean.setScore(score);
		System.out.println("bean retuned by normal process = " + answerBean);

		return answerBean;
	}

	// The entrance to understand the user query and get answer from Neo4j
	// input: the question sentence from users,"姚明身高是多少"
	// output: the answer without answer rewriting, “226cm”
	private AnswerBean answerProcess(AnswerBean answerBean) {

		AnswerRewrite answerRewite = new AnswerRewrite();
		System.out.println("##### entitySet=" + entitySet);

//		if (isQuestion == false) {
//			Debug.printDebug(uniqueID, 4, "knowledge", "the input sentence is not a question");
//			System.out.println("the input sentence is not a question");
//			return answerBean.returnAnswer(answerBean);
//		}


		// TBD: if the sentence does not contain the entity, go through the
		// process of matching value of properties
		System.out.println("PMP.getAnswer: entity = " + entitySet.toString());
		if (entitySet == null || entitySet.isEmpty()) {
			System.out.println("the sentence does not contain entity name and so return empty");
			return answerBean.returnAnswer(answerBean);
		}

		// PatternMatchingResultBean beanPM = new PatternMatchingResultBean();
		String entity = entitySet.get(0);
		// for single entity case
		// if (entitySet.size() == 1) {
		System.out.println("TEMP 1 answerBean=" + answerBean.toString() + ", comments=" + answerBean.getComments());
		System.out.println("TIME 5 - get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter) +", entity="+entity);
		if (entitySet.size() == 1) {
			
			// map<entity, labelList> 
			Map<String, List<String>> entityProcessMap = new HashMap<>();
			if(!NLPUtil.getLabelByEntity(entity).isEmpty()){
				List<String> tempListLabel = NLPUtil.getLabelListByEntity(entity);
				entityProcessMap.put(entity, tempListLabel);
			}
			if(NLPUtil.isASynonymEntity(entity)){
				List<String> tempSynonymList = NLPUtil.getEntitySynonymNormal(entity);
				System.out.println("tempSynonymList = " + tempSynonymList);
				
				for(String tempS : tempSynonymList){
					List<String> tempListLabel = NLPUtil.getLabelListByEntity(tempS);
					entityProcessMap.put(tempS, tempListLabel);
				}
			}
			
			// iterate each label of entity, and get the answer with highest
			// score
//			List<String> listLabel = NLPUtil.getLabelListByEntity(entity);
//			System.out.println("listLabel = " + listLabel);
			
			// String oldSentence = sentence;
			List<AnswerBean> singleEntityAnswerBeanList = new ArrayList<>();
			
			System.out.println("entity Map Process: entityProcessMap="+entityProcessMap);
			for(String eachEntity : entityProcessMap.keySet()){
				List<String> listLabel = entityProcessMap.get(eachEntity);
				String sentence = nerBean.getSentence();
				System.out.println("listLabel = " + listLabel);
				if(!eachEntity.equals(entity)){
					sentence = nerBean.getSentence().toLowerCase().replace(entity, eachEntity);
					System.out.println("synonym entity case, change the sentence to " + sentence);
				}
				System.out.println("entityProcess: sentence is "+sentence);
				
				for (String iLabel : listLabel) {
					if (Tool.isStrEmptyOrNull(sentence)) {
						continue;
					}
					
					// since there maybe more than one record in a label with the same name
					List<String> listKey = DBProcess.getKeyListbyEntity(eachEntity, iLabel);
					for(String iKey : listKey){
						AnswerBean tempBean = new AnswerBean();
						tempBean.setComments(answerBean.getComments());

						PropertyRecognizer propertyRecognizer = new PropertyRecognizer(nerBean);
						tempBean = propertyRecognizer.ReasoningProcess(sentence, iLabel, eachEntity, tempBean, iKey);
						System.out.println("\t ReasoningProcess answerBean = " + tempBean);

						// add the implicationQuestion process here, for now only check
						// the year computing
						if (QuestionClassifier.isKindofQuestion(NLPUtil.removeMoodWord(eachEntity, sentence), QuestionClassifier.implicationQuestionType, "")) {
							tempBean = QuestionClassifier.implicationQuestionProcess(NLPUtil.removeMoodWord(eachEntity, sentence), eachEntity, tempBean);
							// answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer(),
							// 0));
							System.out.println("Implication Qustion: tempBean is " + tempBean.toString());
						}

						if (tempBean.isValid()) {
							singleEntityAnswerBeanList.add(tempBean);
						}

						System.out.println("TEMP answerBean=" + tempBean);
					}
				}
				
			}
			

			

//			// for entity Synonym case
//			if (singleEntityAnswerBeanList.isEmpty()) {
//				if (DictionaryBuilder.getEntitySynonymTable().containsKey(entity)) {
//					String entitySynonym = DictionaryBuilder.getEntitySynonymTable().get(entity);
//					String entitySynonymLabel = NLPUtil.getLabelByEntity(entitySynonym);
//					String entitySynonymSentence = sentence.toLowerCase().replace(entity, entitySynonym);;
//					
//					AnswerBean tempBean = new AnswerBean();
//					tempBean.setComments(answerBean.getComments());
//					PropertyRecognizer propertyRecognizer = new PropertyRecognizer(nerBean);
//					tempBean = propertyRecognizer.ReasoningProcess(entitySynonymSentence, entitySynonymLabel, entitySynonym,
//							tempBean, "");
//					
//					if (QuestionClassifier.isKindofQuestion(entitySynonymSentence, QuestionClassifier.implicationQuestionType,
//							"")) {
//						tempBean = QuestionClassifier.implicationQuestionProcess(userSentence, entitySynonym, tempBean);
//						System.out.println("Implication Qustion: tempBean is " + tempBean.toString());
//					}
//					System.out.println("\t ReasoningProcess answerBean for entity synonym = " + tempBean);
//
//					if (tempBean.isValid()) {
//						singleEntityAnswerBeanList.add(tempBean);
//						System.out.println("TEMP answerBean for entity Synonym = " + tempBean);
//					}
//				}
//			}

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
			List<String> relationDirectNormalWayPathSet = DBProcess.getRelationshipTypeInStraightPath(NLPUtil.getLabelByEntity(entitySet.get(0)),
					entitySet.get(0), NLPUtil.getLabelByEntity(entitySet.get(1)), entitySet.get(1), 1);
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
				List<String> relationDirectReverseWayPathSet = DBProcess.getRelationshipTypeInStraightPath(NLPUtil.getLabelByEntity(entitySet.get(1)),
						entitySet.get(1), NLPUtil.getLabelByEntity(entitySet.get(0)), entitySet.get(0), 1);
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
				List<String> relationNormalWayPathSet = DBProcess.getRelationshipTypeInStraightPath(NLPUtil.getLabelByEntity(entitySet.get(0)),
						entitySet.get(0), NLPUtil.getLabelByEntity(entitySet.get(1)), entitySet.get(1), 2);
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
				List<String> relationReverseWayPathSet = DBProcess.getRelationshipTypeInStraightPath(NLPUtil.getLabelByEntity(entitySet.get(1)),
						entitySet.get(1), NLPUtil.getLabelByEntity(entitySet.get(0)), entitySet.get(0), 2);
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
				List<List<String>> relationConvergePathSet = DBProcess.getRelationshipTypeInConvergePath(NLPUtil.getLabelByEntity(entitySet.get(0)),
						entitySet.get(0), NLPUtil.getLabelByEntity(entitySet.get(1)), entitySet.get(1));
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
				List<List<String>> relationDivergePathSet = DBProcess.getRelationshipTypeInDivergentPath(NLPUtil.getLabelByEntity(entitySet.get(0)),
						entitySet.get(0), NLPUtil.getLabelByEntity(entitySet.get(1)), entitySet.get(1));
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

		// if it is the selective question
		if (QuestionClassifier.isKindofQuestion(userSentence, QuestionClassifier.selectiveQuestionType, "")) {
			System.out.println("\t into selective question, answerBean=" + answerBean);
			answerBean = QuestionClassifier.selectiveQuestionProcess(userSentence, answerBean, nerBean);
			answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer(), 2));
			System.out.println("RETURN of GETANSWER: Selective Qustion: anwerBean is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}

		if (!answerBean.getAnswer().isEmpty() && answerBean.getScore()>=80) {
			// for the highfrequent case
			if (NLPUtil.isInRemoveableMauallyCollectedDict(entity)) {
				answerBean.setScore(0);
			}
			answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer()));
			System.out.println("PM.getAnswer 5: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		} else if (NLPUtil.isInRemoveableAllDict(entity) || NLPUtil.isInDailyUsedWordDict(entity)) {
			System.out.println("PM.getAnswer 5.1: in Removeable Case");
			return answerBean.returnAnswer(answerBean);
		} else {
			// introduction case
//			String strIntroduce = DBProcess.getPropertyValue(entity, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			
			String sentence = nerBean.getSentence();
			String tempEntity = entity;
			if(!NLPUtil.isDBEntity(entity) && NLPUtil.isASynonymEntity(entity)){
				tempEntity = NLPUtil.getEntitySynonymNormal(entity).get(0);
				sentence = nerBean.getSentence().toLowerCase().replace(entity, tempEntity);
				System.out.println("Introduction synonym entity case, change the sentence to " + sentence + " with entity " + tempEntity);
			} else {
				System.out.println("Introduction normal entity case, change the sentence to " + sentence + " with entity " + tempEntity);
			}
			
			String strIntroduce = DBProcess.getEntityIntroduction(tempEntity);
			if (strIntroduce.contains("。"))
				strIntroduce = strIntroduce.substring(0, strIntroduce.indexOf("。"));

			if (!sentence.contains(tempEntity)) {
				System.out.println("userSentence=" + sentence + "++++ tempEntity=" + tempEntity);

				String searchRS = CommonUtil.matchPropertyValue(tempEntity, segWordWithoutStopWord);
				System.out.println("searchRS=" + searchRS);
				if (Tool.isStrEmptyOrNull(searchRS)) {
					return answerBean.returnAnswer(answerBean);
				}
				String oldEntity = searchRS.substring(0, searchRS.indexOf("----####"));
				strIntroduce = searchRS.replace("----####", "是" + tempEntity + "的") + "。" + tempEntity + "是" + strIntroduce;
				answerBean.setScore(QuestionClassifier.isKindofQuestion(
						NLPUtil.removePunctuateMark(sentence.replace(oldEntity, tempEntity)), "Introduction", tempEntity)
								? 50 : 0);
				answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(strIntroduce));
			} else {
				answerBean.setScore(QuestionClassifier.isKindofQuestion(NLPUtil.removePunctuateMark(sentence),
						"Introduction", tempEntity) ? 100 : answerBean.getScore());
				// otherwise, it already has an answer from property recognization
				if(answerBean.getScore() == 100 || answerBean.getScore() == 0){
					answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(strIntroduce));
				}
			}
			
			// to avoid the case of "是什么" in a statement 
			if(nerBean.getQuestionScore() < 20){
				answerBean.setScore(0);
			}
			
			System.out.println("PM.getAnswer 7: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}
	}

	public static void main(String[] args) {
		// NLPProcess nlpProcess = new NLPProcess();
		// NLPProcess.NLPProcessInit();
		DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
		DictionaryBuilder.DictionaryBuilderInit();
		String str = "金毛是什么";
		CUBean bean = new CUBean();
		bean.setText(str);
		bean.setQuestionType("question-info");
		// bean.setQuestionType("debug");
		bean.setScore("50");
		// PatternMatchingProcess mp = new PatternMatchingProcess(bean);
		// AnswerBean bean1 = mp.getAnswer();

		KGAgent agent = new KGAgent(bean);
		AnswerBean bean2 = agent.getAnswer();

		// System.out.println("PM Method: "+ bean1);
		System.out.println("KG Method: " + bean2);
		System.out.println("debug comments=" + bean2.getComments());

	}

}
