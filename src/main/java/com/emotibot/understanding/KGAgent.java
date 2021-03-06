package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> enter into KGAgent, the uniqueID is:" + uniqueID+"********");
		boolean isRewrite = cuBean.isRewrite() ? true: false;
		if (Tool.isStrEmptyOrNull(uniqueID)) {
			uniqueID = "0";
		}

		// add for debug by PM
		if (questionType != null && questionType.equals(CommonConstantName.IS_DEBUG)) {
			System.out.println("DEBUG is TRUE");
			debugFlag = true;
			nerBean.setDebug(true);
			questionScore = 100;
		} else {
			debugFlag = false;
		}
		// System.err.println("questionType="+questionType+", debugFlag =
		// "+debugFlag);

		if (text == null) {
			System.err.println("text is null");
			Debug.printDebug(uniqueID, 2, "knowledge", "init, text is null");
			text = "";
		}

//		nerBean.setOldSentence(CharUtil.trimAndlower(text));	// fix the bad case "你好。"
		
		nerBean.setUniqueID(uniqueID);
		nerBean.setRewrite(isRewrite);

		userSentence = text.toLowerCase();

		if (questionType == null || requestScore == null) {
			Debug.printDebug(uniqueID, 2, "knowledge", "init, question or score is null");
			System.err.println("question or score is null");
			questionType = "";
			requestScore = "";
		} else if (questionType.startsWith(CommonConstantName.IS_QUESTION)) {
			try {
				questionScore = Double.parseDouble(requestScore);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		nerBean.setQuestionScore(questionScore);

		// System.out.println("questionType="+questionType+",
		// questionScore="+questionScore);

		if (userSentence.endsWith(CommonConstantName.IS_QUESTIONMARK_EN) || userSentence.endsWith(CommonConstantName.IS_QUESTIONMARK_CN)) {
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

		Debug.printDebug(uniqueID, 3,"knowledge" , "KGAgent >>>>>> before enter into EntityRecognizer nerBean is:"+ nerBean);
		System.out.println("TIME 3 - before get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
		// change the follow method of getting entity, by the case:
		// "玛丽和马克思的其他中文名叫什么"
		// note:"和" is stop word
		// entitySet = getEntity(NLPProcess.removeStopWord(userSentence));
		Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> before enter into EntityRecogizer, the uniqueID is:" + uniqueID+"********");
		EntityRecognizer entityActor = new EntityRecognizer(nerBean);
		nerBean = entityActor.updateNERBean();
		Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> after return EntityRecogizer, the uniqueID is:" + uniqueID+"********");
		entitySet = nerBean.getEntitySet();
		userSentence = nerBean.getSentence();
		System.out.println("TIME 4 - get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
		System.out.println("KGAgent: bean=" + nerBean.toString());
	}

	public AnswerBean getAnswer() {
		Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> enter into getAnswer() and the uniqueID is:"+ uniqueID);
		AnswerBean answerBean = new AnswerBean();
		if (Tool.isStrEmptyOrNull(nerBean.getOldSentence()) || CharUtil.isNumberFormat(nerBean.getOldSentence())||nerBean.getEntitySet().isEmpty()) {
			System.err.println("PMP.getAnswer: input is empty or is in wrong format");
			return answerBean.returnAnswer(answerBean);
		}

		// Preprocess: removing the senetence begin with words like "你喜欢"
		if(NLPUtil.isStartWithSpecialPrefixWord(userSentence)){
			System.out.println("preprocess return = " + answerBean);
			return answerBean;
		}
		
		//filter some bad case about the sentence 
		Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> enter into new QuestionFilter(nerBean) and the uniqueID is:"+ uniqueID);
		QuestionFilter questionFilter = new QuestionFilter(nerBean);
		answerBean = questionFilter.filterSentence();
		Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> after returnquestionFilter.filterSentence() and the uniqueID is:"+ uniqueID);
		
		if(answerBean.isValid()){
			System.out.println("bean retuned by filter = " + answerBean);
			return answerBean;
		}else{
			// Intention Process
			Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> before enter into IntentionClassifier() and the uniqueID is:"+ uniqueID);
			IntentionClassifier intention = new IntentionClassifier(nerBean);
			answerBean = intention.intentionProcess();
			Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> after return IntentionClassifier() and the uniqueID is:"+ uniqueID);
		}
		
		if (answerBean.isValid()) {
			// if answerBean is valid, which means intention generates it, return this answer
			System.out.println("bean retuned by intention = " + answerBean);
			return answerBean;
		} else {
			Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>> before enter into the method answerProcess() ");
			answerBean = answerProcess(answerBean);
		}
		
		double score = answerBean.getScore();
		System.out.println("pre score==="+score);
		// if it is not a question, then lower the score of the answer
		// since if there is another answer from other module, the answer from KG with lower score will not be selected
		/*if(nerBean.getQuestionScore() < 5){
			Debug.printDebug(uniqueID, 3, "knowledge", "KGAgent >>>>>>  get question score is "+ nerBean.getQuestionScore());
			score = 0;
		}*/
		
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
		
		// add high frequent process in property recognization
		if(NLPUtil.isInRemoveableMauallyCollectedDict(entity)){
			return answerBean.returnAnswer(answerBean);
		}
		
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
					System.out.println("entityProcess: synonym entity case, change the sentence to " + sentence);
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
						tempBean = propertyRecognizer.ReasoningProcess(sentence, iLabel, eachEntity, tempBean, iKey, false);
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
			Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 return back ReasoningProcess() method and answerBean is: "+ answerBean);
			System.out.println("\t Single Entity answerBean = " + answerBean);

			System.out.println("TIME 6 - Single Entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));

		} else if (QuestionClassifier.isRelationshipQuestion(userSentence)) {
			Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 userSentence isRelationshipQuestion");
			List<String> relationDirectNormalWayPathSet = DBProcess.getRelationshipTypeInStraightPath(NLPUtil.getLabelByEntity(entitySet.get(0)),
					entitySet.get(0), NLPUtil.getLabelByEntity(entitySet.get(1)), entitySet.get(1), 1);
			System.out
					.println("TIME 8 - get relationships >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));

			String answerRelation = "";
			if (!relationDirectNormalWayPathSet.isEmpty()) {
				String directNormalWayRelation = entitySet.get(1) + CommonConstantName.IS_SHI + entitySet.get(0);
				for (String s : relationDirectNormalWayPathSet) {
					directNormalWayRelation += CommonConstantName.STOPWORD1 + s;
				}
				answerRelation = directNormalWayRelation;
				System.out.println("\t directNormalWayRelation = " + directNormalWayRelation);
			}

			if (answerRelation.isEmpty()) {
				List<String> relationDirectReverseWayPathSet = DBProcess.getRelationshipTypeInStraightPath(NLPUtil.getLabelByEntity(entitySet.get(1)),
						entitySet.get(1), NLPUtil.getLabelByEntity(entitySet.get(0)), entitySet.get(0), 1);
				if (!relationDirectReverseWayPathSet.isEmpty()) {
					System.out.println("relationReverseWayPathSet=" + relationDirectReverseWayPathSet);
					String directReverseWayRelation = entitySet.get(0) + CommonConstantName.IS_SHI + entitySet.get(1);
					for (String s : relationDirectReverseWayPathSet) {
						directReverseWayRelation += CommonConstantName.STOPWORD1 + s;
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
					String normalWayRelation = entitySet.get(1) + CommonConstantName.IS_SHI + entitySet.get(0);
					for (String s : relationNormalWayPathSet) {
						normalWayRelation += CommonConstantName.STOPWORD1 + s;
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
					String reverseWayRelation = entitySet.get(0) + CommonConstantName.IS_SHI + entitySet.get(1);
					for (String s : relationReverseWayPathSet) {
						reverseWayRelation += CommonConstantName.STOPWORD1 + s;
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
					String convergeRelation = entitySet.get(0) + CommonConstantName.IS_HE + entitySet.get(1) + CommonConstantName.STOPWORD1;
					for (List<String> listStr : relationConvergePathSet) {
						convergeRelation += listStr.get(1) + CommonConstantName.IS_ALLIS + listStr.get(0) + CommonConstantName.IS_COMMA;
					}
					convergeRelation = convergeRelation.substring(0, convergeRelation.length() - 1);
					// for now, return the longest one
					convergeRelation = Tool.getLongestStringInArray(convergeRelation.split(CommonConstantName.IS_COMMA));
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
					String divergeRelation = entitySet.get(0) + CommonConstantName.IS_HE + entitySet.get(1) + CommonConstantName.IS_ALLIS;
					for (List<String> listStr : relationDivergePathSet) {
						divergeRelation += listStr.get(0) + CommonConstantName.STOPWORD1 + listStr.get(1) + CommonConstantName.IS_COMMA;
					}
					divergeRelation = divergeRelation.substring(0, divergeRelation.length() - 1);
					// for now, return the longest one
					divergeRelation = Tool.getLongestStringInArray(divergeRelation.split(CommonConstantName.IS_COMMA));
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
			Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 userSentence is selectiveQuestionType");
			System.out.println("\t into selective question, answerBean=" + answerBean);
			answerBean = QuestionClassifier.selectiveQuestionProcess(userSentence, answerBean, nerBean);
			answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer(), 2));
			System.out.println("RETURN of GETANSWER: Selective Qustion: anwerBean is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}

		if (!answerBean.getAnswer().isEmpty() && answerBean.getScore()>=80) {
			Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 enter into: if (!answerBean.getAnswer().isEmpty() && answerBean.getScore()>=80) and the answerBean is: "+ answerBean);
			// for the highfrequent case
			if (NLPUtil.isInRemoveableMauallyCollectedDict(entity)) {
				Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 the entity "+ entity+" is InRemoveableMauallyCollectedDict");
				answerBean.setScore(0);
			}
			answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer()));
			if(answerBean.getScore()>=50 && answerBean.getScore()<80){
				Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 the score is: "+ answerBean.getScore()+"  >>>enter into if(answerBean.getScore()>=50 && answerBean.getScore()<80)");
				System.out.println("adjust score in property case from "+answerBean.getScore()+" to 80");
				answerBean.setScore(80);
			}
			System.out.println("PM.getAnswer 5: the returned anwer is " + answerBean.toString());
			Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 return the answerBean: "+ answerBean);
			return answerBean.returnAnswer(answerBean);
		} else if (NLPUtil.isInRemoveableAllDict(entity) || NLPUtil.isInDailyUsedWordDict(entity)) {
			Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 enter into  (NLPUtil.isInRemoveableAllDict(entity) || NLPUtil.isInDailyUsedWordDict(entity)) and the entity is: "+ entity);
			System.out.println("PM.getAnswer 5.1: in Removeable Case");
			return answerBean.returnAnswer(answerBean);
		} else {
			// introduction case
//			String strIntroduce = DBProcess.getPropertyValue(entity, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			Debug.printDebug(nerBean.getUniqueID(), 3, "knowledge", "KGAgent >>>>>> model_6 userSentence is other case");
			String sentence = nerBean.getSentence();
			String tempEntity = entity;
			if(!NLPUtil.isDBEntity(entity) && NLPUtil.isASynonymEntity(entity)){
				tempEntity = NLPUtil.getEntitySynonymNormal(entity).get(0);
				sentence = nerBean.getSentence().toLowerCase().replace(entity, tempEntity);
				System.out.println("Introduction synonym entity case, change the sentence to " + sentence + " with entity " + tempEntity);
			} else {
				System.out.println("Introduction normal entity case, change the sentence to " + sentence + " with entity " + tempEntity);
			}
			
			
			String strIntroduce = DBProcess.getEntityIntroduction(tempEntity,NLPUtil.getLabelByEntity(tempEntity));
			if (strIntroduce.contains(CommonConstantName.IS_JUHAO_CN))
				strIntroduce = strIntroduce.substring(0, strIntroduce.indexOf(CommonConstantName.IS_JUHAO_CN));

			if (!sentence.contains(tempEntity)) {
				System.out.println("userSentence=" + sentence + "++++ tempEntity=" + tempEntity);

				String searchRS = CommonUtil.matchPropertyValue(tempEntity, segWordWithoutStopWord);
				System.out.println("searchRS=" + searchRS);
				if (Tool.isStrEmptyOrNull(searchRS)) {
					return answerBean.returnAnswer(answerBean);
				}
				String oldEntity = searchRS.substring(0, searchRS.indexOf(CommonConstantName.TEMPLATE_BEGIN));
				strIntroduce = searchRS.replace(CommonConstantName.TEMPLATE_BEGIN, CommonConstantName.IS_SHI + tempEntity + CommonConstantName.STOPWORD1) + CommonConstantName.IS_JUHAO_CN + tempEntity + CommonConstantName.IS_SHI + strIntroduce;
				answerBean.setScore(QuestionClassifier.isKindofQuestion(
						NLPUtil.removePunctuateMark(sentence.replace(oldEntity, tempEntity)), CommonConstantName.IS_INTRODUCTION, tempEntity)
								? 50 : 0);
				answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(strIntroduce));
			} else {
				boolean isKindOfQuestion = QuestionClassifier.isKindofQuestion(NLPUtil.removePunctuateMark(sentence),CommonConstantName.IS_INTRODUCTION, tempEntity);
				
				if(isKindOfQuestion){

					/**
					 * 开始处理rewrite 的多义词情况
					 */
					IntentionClassifier intentionClassifier =  new IntentionClassifier();
					List<String> labelList = NLPUtil.getLabelListByEntity(tempEntity);
					List<String> finalLabelList3 = intentionClassifier.getFinalLabelListOfCase1(labelList);
					if (finalLabelList3.size() > 1) {
						return intentionClassifier.getAnswerOfCase1(finalLabelList3,tempEntity);
					}
					
					answerBean.setScore(100);
				}else {
					answerBean.setScore(answerBean.getScore());
				}
				
				// otherwise, it already has an answer from property recognization
				if(answerBean.getScore() == 100 || answerBean.getScore() == 0){
					//add prefix introduction if sentence start with [你，小影]，[认识，知道]
					String introWord = NLPUtil.isContainsInIntroductionPrefixWord(sentence);
					if(!introWord.isEmpty() && !introWord.equals("")){
						strIntroduce = answerRewite.rewriteAnser4IntroBegin(strIntroduce, introWord);
					}
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
		TemplateEntry.TemplateEntryInit();
		String str = "你知道魔兽吗？";
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
