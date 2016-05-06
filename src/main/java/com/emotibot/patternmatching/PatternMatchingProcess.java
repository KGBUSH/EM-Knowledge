package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.emotibot.WebService.AnswerBean;
import com.emotibot.answerRewrite.AnswerRewrite;
import com.emotibot.common.Common;
import com.emotibot.log.LogService;
import com.emotibot.solr.SolrUtil;
import com.emotibot.solr.Solr_Query;
import com.emotibot.template.TemplateEntry;
import com.emotibot.template.TemplateProcessor;
import com.emotibot.util.CUBean;
import com.emotibot.util.CharUtil;
import com.emotibot.util.StringLengthComparator;
import com.emotibot.util.Tool;
import com.emotibot.Debug.Debug;
import com.hankcs.hanlp.seg.common.Term;

public class PatternMatchingProcess {

	final TemplateProcessor questionClassifier = new TemplateProcessor("QuestionClassifier");

	final String introductionQuestionType = "IntroductionQuestion@:";
	final String strictIntroductionQuestionType = "StrictIntroductionQuestion@:";
	final String selectiveQuestionType = "SelectiveQuestion@:";
	final String implicationQuestionType = "ImplicationQuestion@:";
	// final String relationshipQuestionType = "RelationshipQuestion@:";

	private String userSentence;
	private List<Term> segPos;
	private List<String> segWordWithoutStopWord;
	private List<String> entitySet;
	private boolean isQuestion = false;
	private long timeCounter = System.currentTimeMillis();
	private String uniqueID = "";

	private boolean debugFlag = false;

	// public PatternMatchingProcess(String str) {
	// // Debug.printDebug()
	//
	// if (str == null) {
	// System.err.println("text is null");
	// str = "";
	// }
	// userSentence = str;
	// isQuestion = true;
	// // NLPResult tnNode = NLPSevice.ProcessSentence(userSentence,
	// // NLPFlag.SegPos.getValue());
	// // segPos = tnNode.getWordPos();
	// segPos = NLPProcess.getSegWord(userSentence);
	// segWordWithoutStopWord = new ArrayList<>();
	// for (int i = 0; i < segPos.size(); i++) {
	// String segWord = segPos.get(i).word.trim();
	// if (!NLPProcess.isStopWord(segWord)) {
	// segWordWithoutStopWord.add(segWord);
	// }
	// }
	// System.out.println("TIME 1 - before get entity >>>>>>>>>>>>>> " +
	// (System.currentTimeMillis() - timeCounter));
	// // entitySet = getEntity(NLPProcess.removeStopWord(userSentence));
	// entitySet = getEntity(userSentence);
	// userSentence = changeEntitySynonym(entitySet, userSentence);
	// System.out.println("TIME 2 - get entity >>>>>>>>>>>>>> " +
	// (System.currentTimeMillis() - timeCounter));
	//
	// System.out.println("Constructor: userSentence=" + userSentence);
	// System.out.println("Constructor: isQuestion=" + isQuestion);
	// System.out.println("Constructor: segPos=" + segPos);
	// System.out.println("Constructor: segWordWithoutStopWord=" +
	// segWordWithoutStopWord);
	// System.out.println("Constructor: entitySet=" + entitySet);
	// }

	public PatternMatchingProcess(CUBean cuBean) {
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
		} else if (questionScore >= 0.3) {
			isQuestion = true;
		}
		userSentence = NLPProcess.removePunctuateMark(userSentence);

		System.out.println("userSentence=" + userSentence + ", isQuestion=" + isQuestion);
		segPos = NLPProcess.getSegWord(userSentence);
		System.out.println("Constructor: segPos=" + segPos);
		segWordWithoutStopWord = new ArrayList<>();
		for (int i = 0; i < segPos.size(); i++) {
			String segWord = CharUtil.trim(segPos.get(i).word);
			System.out.println("segWord=" + segWord);
			if (!NLPProcess.isStopWord(segWord)) {
				segWordWithoutStopWord.add(segWord);
			}
		}
		System.out.println("TIME 3 - before get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
		// change the follow method of getting entity, by the case:
		// "玛丽和马克思的其他中文名叫什么"
		// note:"和" is stop word
		// entitySet = getEntity(NLPProcess.removeStopWord(userSentence));
		entitySet = getEntity(userSentence.toLowerCase());
		userSentence = changeEntitySynonym(entitySet, userSentence);
		removeAbnormalEntity(entitySet);
		System.out.println("TIME 4 - get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));

		System.out.println("Constructor: userSentence=" + userSentence);
		System.out.println("Constructor: isQuestion=" + isQuestion);
		System.out.println("Constructor: segPos=" + segPos);
		System.out.println("Constructor: segWordWithoutStopWord=" + segWordWithoutStopWord);
		System.out.println("Constructor: entitySet=" + entitySet);

	}

	// remove stopword and other abnormal word in entity
	private void removeAbnormalEntity(List<String> entitySet) {
		Iterator<String> it = entitySet.iterator();
		while (it.hasNext()) {
			String tempEntity = it.next();

			if (NLPProcess.isInRemoveableDict(tempEntity)) {
				System.out.println("remove entity:" + tempEntity);
				it.remove();
			}
		}
	}

	// if the sentnece contain a alias, change to wiki entity name
	// input: “甲肝是什么” output: “甲型病毒性肝炎是什么”
	private String changeEntitySynonym(List<String> entitySet, String sentence) {
		System.out.println("changeEntitySynonym: entitySet=" + entitySet + ",sentence=" + sentence);
		for (String entity : entitySet) {
			if (NLPProcess.hasEntitySynonym(entity)) {
				List<String> list = NLPProcess.getSynonymnEntityList(entity);
				System.out.println("entity=" + entity + ", synonymList=" + list);
				String oldEntity = "";
				for (String s : list) {
					if (sentence.contains(s)) {
						oldEntity = s;
						break;
					}
				}
				if (oldEntity.isEmpty() || !sentence.contains(entity)) {
					LogService.printLog(uniqueID, "PatternMatching.changeEntitySynonym", "entity=" + entity);
					System.err.println("PatternMatching.changeEntitySynonym: entity=" + entity);
				}
				// String oldEntity =
				// NLPProcess.getEntitySynonymReverse(entity).toLowerCase();
				if (!oldEntity.isEmpty()) {
					sentence = sentence.toLowerCase().replace(oldEntity, entity);
				}
				System.out.println("changeEntitySynonym change : s = " + entity + ", oldEntity=" + oldEntity
						+ "; sentence=" + sentence);
			}
		}
		return sentence;
	}

	// The entrance to understand the user query and get answer from Neo4j
	// input: the question sentence from users,"姚明身高是多少"
	// output: the answer without answer rewriting, “226cm”
	public AnswerBean getAnswer() {

		String sentence = userSentence;
		AnswerBean answerBean = new AnswerBean();
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getAnswer: input is empty");
			return answerBean.returnAnswer(answerBean);
		}

		AnswerRewrite answerRewite = new AnswerRewrite();

		if (entitySet.size() == 1 && entitySet.get(0).equals(sentence)) {
			System.out.println("Single Entity Case: entity=" + entitySet.get(0));
			if(DBProcess.getEntityLabel(entitySet.get(0)).equals("catchword")){
				System.out.println("catchword Case, and abord， the returned anwer is " + answerBean.toString());
				return answerBean;
			}
			
			String tempStrIntroduce = DBProcess.getPropertyValue(entitySet.get(0), Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			if (tempStrIntroduce.contains("。"))
				tempStrIntroduce = tempStrIntroduce.substring(0, tempStrIntroduce.indexOf("。"));
			answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(tempStrIntroduce));
			answerBean.setScore(100);
			System.out.println("PM.getAnswer 0.1: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}

		if (isQuestion == false) {
			Debug.printDebug(uniqueID, 4, "knowledge", "the input sentence is not a question");
			return answerBean.returnAnswer(answerBean);
		}

		if (Common.KG_DebugStatus || debugFlag) {
			String tempLabel = "";
			if (!entitySet.isEmpty()) {
				tempLabel = DBProcess.getEntityLabel(entitySet.get(0));
			}
			String debugInfo = "userSentence=" + userSentence + "; entitySet=" + entitySet + "; label=" + tempLabel;
			Debug.printDebug("123456", 1, "KG", debugInfo);
			answerBean.setComments(debugInfo);
		}

		// 1. get the entity and Revise by template
		if (entitySet.size() > 2) {
			// check for 4/15 temporarily, may extent later
			System.err.println("NOTES: PM.getAnswer: there are more than two entities");
		}

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

				// print debug log
				String debugInfo = "template process: ilabel:" + iLabel + " from:" + oldSentence + " to:"
						+ tempSentence;
				System.out.println(debugInfo);
				if (Common.KG_DebugStatus || debugFlag) {
					Debug.printDebug("123456", 1, "KG", debugInfo);
					answerBean.setComments(debugInfo);
				} else {
					Debug.printDebug(uniqueID, 3, "KG", debugInfo);
				}

				if (Tool.isStrEmptyOrNull(tempSentence)) {
					continue;
				}
				AnswerBean tempBean = new AnswerBean();
				tempBean = ReasoningProcess(tempSentence, iLabel, entity, tempBean);
				System.out.println("\t ReasoningProcess answerBean = " + tempBean);

				// add the implicationQuestion process here, for now only check
				// the year computing
				if (isKindofQuestion(userSentence, implicationQuestionType, "")) {
					tempBean = implicationQuestionProcess(userSentence, entity, tempBean);
					// answerBean.setAnswer(answerRewite.rewriteAnswer(answerBean.getAnswer(),
					// 0));
					System.out.println("Implication Qustion: tempBean is " + tempBean.toString());
				}

				if (tempBean.isValid()) {
					singleEntityAnswerBeanList.add(tempBean);
				}
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

			// sentence = TemplateEntry.templateProcess(entity, iLabel,
			// sentence, uniqueID);

			// print debug log
			// String debugInfo = "template process: from:" + oldSentence + "
			// to:" + sentence;
			// if (Common.KG_DebugStatus || debugFlag) {
			// Debug.printDebug("123456", 1, "KG", debugInfo);
			// answerBean.setComments(debugInfo);
			// } else {
			// Debug.printDebug(uniqueID, 3, "KG", debugInfo);
			// }
			// System.out.println("TIME 6 - get entity >>>>>>>>>>>>>> " +
			// (System.currentTimeMillis() - timeCounter));
			//
			// if (Tool.isStrEmptyOrNull(sentence)) {
			// System.err.println("the sentence become empty after template
			// process");
			// return answerBean.returnAnswer(answerBean);
			// }
			//
			// // answerBean = mutlipleReasoningProcess(sentence, entity);
			// answerBean = ReasoningProcess(sentence, entity, answerBean);

		} else if (isRelationshipQuestion(userSentence)) {
			List<String> relationDirectNormalWayPathSet = DBProcess.getRelationshipTypeInStraightPath("",
					entitySet.get(0), "", entitySet.get(1), 1);
			// System.out.println("\n\t relationNormalWayPathSet = " +
			// relationNormalWayPathSet
			// + "\n\t relationReverseWayPathSet=" + relationReverseWayPathSet +
			// "\n\t relationConverge="
			// + relationConvergePathSet + "\n\t relationDiverge =" +
			// relationDivergePathSet);
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
		if (isKindofQuestion(userSentence, selectiveQuestionType, "")) {
			answerBean = selectiveQuestionProcess(userSentence, answerBean);
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
				localAnswer = matchPropertyValue(entity, segWordWithoutStopWord).replace("----####", "是" + entity + "的")
						+ "。" + entity + "是";
				// System.out.println("segWordWithoutStopWord="+segWordWithoutStopWord+",
				// tempProp="+tempProp+", replacePro="+replaceProp);
			}
			String strIntroduce = DBProcess.getPropertyValue(entity, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			if (strIntroduce.contains("。"))
				strIntroduce = strIntroduce.substring(0, strIntroduce.indexOf("。"));
			localAnswer += strIntroduce;
			answerBean.setAnswer(answerRewite.rewriteAnswer4Intro(localAnswer));
			// answerBean.setScore(
			// isKindofQuestion(NLPProcess.removePunctuateMark(userSentence),
			// introductionQuestionType, "") ? 100
			// : 0);
			answerBean.setScore(
					isKindofQuestion(NLPProcess.removePunctuateMark(userSentence), introductionQuestionType, entity)
							? 100 : 0);
			if (isQuestion == false) {
				answerBean.setScore(0);
			}
			System.out.println("PM.getAnswer 7: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}
	}

	// to match a segword in sentence with some value of a entity.
	String matchPropertyValue(String entity, List<String> segWord) {
		String rs = "";
		Map<String, Object> mapPropValue = DBProcess.getEntityPropValueMap("", entity);

		// if a value contain a segword, then return the key which refer to the
		// value
		for (Object value : mapPropValue.values()) {
			for (String s : segWord) {
				if (value.toString().contains(s)) {
					for (String key : mapPropValue.keySet()) {
						if (value.equals(mapPropValue.get(key))) {
							if (key.equals(Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME)) {
								continue; // if the word comes from
											// introduction, remove
							}
							System.out.println(
									"\t matchPropertyValue: key=" + key + ", value=" + value + ", segword=" + s);
							return s + "----####" + key;
						}
					}
				}
			}
		}

		return rs;
	}

	// identify the entities in a sentence by SimpleMatching, NLP, Solr
	private List<String> getEntity(String sentence) {
		System.out.println("PMP.getEntity: sentence=" + sentence);
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getEntity: input is empty");
			return null;
		}

		System.out.println("segPos=" + segPos);
		System.out.println("segWordWithoutStopWord=" + segWordWithoutStopWord);

		List<String> rsEntity = new ArrayList<>();
		List<String> simpleMatchEntity = NLPProcess.getEntitySimpleMatch(sentence);
		List<String> nlpEntity = NLPProcess.getEntityByNLP(segPos);
		System.out.println("\t simpleMatchingEntity=" + simpleMatchEntity + "\n\t nlpEntity=" + nlpEntity);

		if (isTwoListsEqual(simpleMatchEntity, nlpEntity)) {
			List<String> solrEntity = getEntityBySolr(sentence, nlpEntity, segWordWithoutStopWord);
			System.out.println("\t solrEntity with entity input=" + solrEntity);

			if (simpleMatchEntity.isEmpty()) {
				if (solrEntity.isEmpty())
					return rsEntity;

				String strEntity = solrEntity.get(0);
				if (!matchPropertyValue(strEntity, segWordWithoutStopWord).isEmpty()) {
					rsEntity.add(strEntity);
				}

				System.out.println("case: 0: rsEntity=" + rsEntity);
				return rsEntity;
			}

			if (simpleMatchEntity.size() == 1) {
				rsEntity = simpleMatchEntity;
				System.out.println("case: 1: rsEntity=" + rsEntity);
				return rsEntity;
			}

			if (isRelationshipQuestion(sentence)) {
				// case: 叶莉和姚明的女儿是谁？
				rsEntity.add(simpleMatchEntity.get(0));
				rsEntity.add(simpleMatchEntity.get(1));
				System.out.println("case: 2: rsEntity=" + rsEntity);
				return rsEntity;
			} else {
				// rsEntity.add(simpleMatchEntity.get(0));
				// change simple match set as source for fixing case:
				// "百变小樱是哪种动漫" and "姚明的老婆的身高是多少"
				List<String> tempList = getIntersectionOfTwoLists(simpleMatchEntity, solrEntity,
						simpleMatchEntity.size());

				for (String s : tempList) {
					if (NLPProcess.isEntityPM(s)) {
						rsEntity.add(s);
						break;
					}
				}
				if (rsEntity.isEmpty()) {
					// bug fixing for the case of "黄金矿工哪年发行"
					rsEntity.add(simpleMatchEntity.get(0));
					// rsEntity.add(getSimilarEntityFromTwoLists(solrEntity,
					// simpleMatchEntity));
					System.err.println("case check in case 2.5=" + rsEntity);
					// Debug.printDebug(uniqueID, 2, "knowledge", "case check in
					// case 2.5=" + rsEntity);
				}
				System.out.println("case: 2.5: rsEntity=" + rsEntity);
				return rsEntity;
			}
		} else {
			// simeple Matching Entity != nlpEntity
			if (!nlpEntity.isEmpty() && simpleMatchEntity.isEmpty()) {
				System.err.println("simple Matching is empty but nlp is not");
				return nlpEntity;
			}

			if (nlpEntity.isEmpty()) {
				List<String> solrEntity = getEntityBySolr(sentence, null, segWordWithoutStopWord);
				System.out.println("\t solrEntity without entity input=" + solrEntity);

				if (simpleMatchEntity.size() == 1) {
					if (solrEntity.isEmpty()) {
						rsEntity = simpleMatchEntity;
						System.out.println("case: 2.7: rsEntity=" + rsEntity);
						return rsEntity;
					} else if (hasPropertyInSentence(sentence, "", simpleMatchEntity.get(0))) {
						// case: 猫猫是什么科的？
						rsEntity = simpleMatchEntity;
						System.out.println("case: 3: rsEntity=" + rsEntity);
						return rsEntity;
					} else {
						System.out.println("case 4::::: solrEntity=" + solrEntity);
						if (solrEntity.contains(simpleMatchEntity.get(0))) {
							// 私人订制票房有多少
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 4.1: rsEntity=" + rsEntity);
						} else {
							// case: 熊猫明是谁？
							rsEntity.add(solrEntity.get(0));
							System.out.println("case: 4.2: rsEntity=" + rsEntity);
						}
						return rsEntity;
					}
				} else {
					// size of simple matching is larger than 1
					if (solrEntity.size() < 2) {
						rsEntity = simpleMatchEntity;
						System.out.println("case: 4.7: rsEntity=" + rsEntity);
						return rsEntity;
					} else if (isRelationshipQuestion(sentence)) {
						rsEntity.add(solrEntity.get(0));
						rsEntity.add(solrEntity.get(1));
						System.out.println("case: 5: rsEntity=" + rsEntity);
						return rsEntity;
					} else {
						if (solrEntity.contains(simpleMatchEntity.get(0))) {
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 6.1: rsEntity=" + rsEntity);
						} else {
							rsEntity.add(solrEntity.get(0));
							System.out.println("case: 6.2: rsEntity=" + rsEntity);
						}
						return rsEntity;
					}
				}
			} else {
				// nlp is not empty, return the intersection among the results
				// by three methods
				List<String> mergeEntity = mergeTwoLists(simpleMatchEntity, nlpEntity);
				List<String> solrEntity = getEntityBySolr(sentence, mergeEntity, segWordWithoutStopWord);
				System.out.println("\t solrEntity with entity input=" + solrEntity + ",\n mergeEntity=" + mergeEntity);

				if (simpleMatchEntity.size() == 1) {
					if (solrEntity.isEmpty()) {
						rsEntity = simpleMatchEntity;
						System.out.println("case: 6.5: rsEntity=" + rsEntity);
						return rsEntity;
					} else {
						if (solrEntity.contains(simpleMatchEntity.get(0))) {
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 7.1: rsEntity=" + rsEntity);
						} else {
							rsEntity.add(solrEntity.get(0));
							System.out.println("case: 7.2: rsEntity=" + rsEntity);
						}
						return rsEntity;
					}
				} else {
					// size of simple matching is larger than 1
					if (isRelationshipQuestion(sentence)) {
						if (solrEntity.size() < 2) {
							rsEntity.add(simpleMatchEntity.get(0));
							rsEntity.add(simpleMatchEntity.get(1));
							System.out.println("case: 7.5: rsEntity=" + rsEntity);
							return rsEntity;
						} else {
							rsEntity = getIntersectionOfTwoLists(solrEntity, mergeEntity, 2);
							System.out.println("case: 8: rsEntity=" + rsEntity);
							return rsEntity;
						}
					} else {
						if (solrEntity.isEmpty()) {
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 8.5: rsEntity=" + rsEntity);
							return rsEntity;
						} else {
							rsEntity = getIntersectionOfTwoLists(solrEntity, mergeEntity, 1);
							if (rsEntity.isEmpty()) {
								System.out.println("rsEntity is empty");
								rsEntity.add(simpleMatchEntity.get(0));
							}

							System.out.println("case: 9: rsEntity=" + rsEntity);
							return rsEntity;
						}
					}
				}
			}
		}
	}

	// get the number elements from souce /\ reference
	private List<String> getIntersectionOfTwoLists(List<String> source, List<String> reference, int number) {
		List<String> rsSet = new ArrayList<>();
		for (String s : source) {
			if (reference.contains(s)) {
				rsSet.add(s);
				number--;
			}
			if (number == 0) {
				break;
			}
		}
		return rsSet;
	}

	// get the similar element of two lists
	// if a element in source contain or is contained by another element in
	// solorEntity, return the first element.
	private String getSimilarEntityFromTwoLists(List<String> solrEntity, List<String> sourceEntity) {
		for (String ss : solrEntity) {
			for (String rs : sourceEntity) {
				if (rs.length() >= ss.length() && rs.contains(ss)) {
					return rs;
				}
				if (rs.length() < ss.length() && ss.contains(rs)) {
					return rs;
				}
			}
		}
		return "";
	}

	// Multi-level Reasoning Understanding
	private AnswerBean ReasoningProcess(String sentence, String label, String entity, AnswerBean answerBean) {
		System.out.println(
				"PMP.ReasoningProcess INIT: sentence=" + sentence + ", entity =" + entity + ", bean is " + answerBean);
		// Debug.printDebug(uniqueID, 3, "knowledge", "PMP.ReasoningProcess:
		// sentence=" + templateSentence + ", entity ="
		// + entity + ", bean is " + answerBean.toString());
		// to be checked later

		if (!sentence.contains(entity)) {
			System.err.println("Sentence does not contain entity");
			Debug.printDebug(uniqueID, 2, "knowledge", "Sentence does not contain entity");
			return answerBean.returnAnswer(answerBean);
		}
		String sentenceNoEntity = sentence.substring(0, sentence.indexOf(entity))
				+ sentence.substring(sentence.indexOf(entity) + entity.length());
		System.out.println("\t new sentence is::::" + sentenceNoEntity);

		Map<String, String> relationMap = this.getRelationshipSet(label, entity);
		System.out.println("\t relationMap = " + relationMap);

		// split the sentence by the entities to get candidates
		// get candidates by splitting the sentence by entities and stopwords.
		List<String> candidateSet = this.getCandidateSet(sentence, entity);
		List<String> candidateSetbyStopWord = this.getCandidateSetbyStopWord(candidateSet);
		System.out.println("PMP.ReasoningProcess: candidateSet = " + candidateSet);
		System.out.println("PMP.ReasoningProcess: candidateSetbyStopWord = " + candidateSetbyStopWord);

		// get all the property of the entity
		Map<String, String> propMap = this.getPropertyNameSet(label, entity);
		System.out.println("PMP.ReasoningProcess: propMap = " + propMap);

		// get the matched properties by pattern matching method
		// first get the property by NOT segPos with NO stopword;
		// if match null, then by NOT segPos with stopword
		// if match null, then by segPos with stopword
		List<PatternMatchingResultBean> listPMBean = this.matchPropertyFromSentence(candidateSetbyStopWord, propMap);

		// add for introduction questions
		if (listPMBean.isEmpty() && isKindofQuestion(userSentence, introductionQuestionType, "")) {
			System.out.println("\t EndOfRP introudction case @@ return case 0.0, answer=" + answerBean);
			// does not match property, score decreases
			answerBean.setScore(answerBean.getScore() / 2);
			return answerBean.returnAnswer(answerBean);
		}

		if (listPMBean.isEmpty()) {
			listPMBean = this.matchPropertyFromSentence(candidateSet, propMap);
			System.out.println("PMP.ReasoningProcess: get ListBean not by StopWord = " + listPMBean);
		}
		if (listPMBean.isEmpty()) {
			listPMBean = this.matchPropertyFromSentence(this.getCandidateSetbyNLP(candidateSet), propMap);
			System.out.println("PMP.ReasoningProcess: get ListBean by NLP = " + listPMBean);
		}

		PatternMatchingResultBean implicationBean = ImplicationProcess.checkImplicationWord(sentence);
		if (implicationBean.isValid())
			listPMBean.add(implicationBean);
		System.out.println("\t listPMBean=" + listPMBean);

		if (listPMBean.isEmpty()) {
			System.out.println("\t EndOfRP @@ return case 0, answer=" + answerBean);
			// does not match property, score decreases
			// remove for the case: “姚明的初中在哪里”
			// answerBean.setScore(answerBean.getScore() / 2);
			return answerBean.returnAnswer(answerBean);
		} else if (listPMBean.size() == 1) {
			String prop = listPMBean.get(0).getAnswer();
			String answer = "";
			System.out.println("\t\t\t#### before Implication ");
			if (ImplicationProcess.isImplicationWord(prop)) {
				System.out.print("\t\t\t#### Implication ");
				answer = ImplicationProcess.getImplicationAnswer(sentence, entity, prop);
				if (Tool.isStrEmptyOrNull(answer))
					listPMBean.get(0).setScore(0);
				System.out.println("answer = " + answer);
			} else {
				answer = DBProcess.getPropertyValue(label, entity, prop);
			}
			answerBean.setAnswer(answer);
			answerBean.setProperty(prop);
			// answerBean.setValid(true);
			answerBean.setScore(listPMBean.get(0).getScore());
			String oldWord = listPMBean.get(0).getOrignalWord();
			answerBean.setOriginalWord(oldWord);
			System.out.println("\t answer = " + answerBean);

			if (relationMap.containsKey(prop)) {
				// use the new answer as new entity
				String newDBEntity = DBProcess.getEntityByRelationship(label, entity, prop);
				String newSentence = sentenceNoEntity;
				if (!Tool.isStrEmptyOrNull(oldWord)) {
					newSentence = sentenceNoEntity.replace(oldWord, newDBEntity);
				}
				System.out.println("-----> case 1 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				System.out.println("prop:" + prop + ", new sentence:" + newSentence);
				return ReasoningProcess(newSentence, label, newDBEntity, answerBean);
			} else {
				System.out.println("\t EndOfRP  @@ return case 1, answer=" + answerBean);
				return answerBean.returnAnswer(answerBean);
			}
		} else {
			// in the case of multiple props, find a way out
			String answer = "";
			double score = 100;

			boolean furtherSeach = false;
			String prop = "";

			for (PatternMatchingResultBean b : listPMBean) {
				String queryAnswer = DBProcess.getPropertyValue(label, entity, b.getAnswer());
				if (relationMap.containsKey(b.getAnswer())) {
					furtherSeach = true;
					prop = b.getAnswer();
					answerBean.setAnswer(queryAnswer);
					answerBean.setProperty(b.getAnswer());
					// answerBean.setValid(true);
					answerBean.setScore(b.getScore());
					String oldWord = b.getOrignalWord();
					answerBean.setOriginalWord(oldWord);
					break;
				} else {
					answer += entity + "的" + b.getAnswer() + "是" + queryAnswer + "；";
					score *= b.getScore() / 100;
					System.out.print(" * " + b.getScore() + "/100 ");
				}
			}

			if (furtherSeach == true) {
				String newDBEntity = DBProcess.getEntityByRelationship(label, entity, prop);
				String newLabel = label; // TBD, should change later
				System.out.println("-----> case 2 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				return ReasoningProcess(sentenceNoEntity.replace(answerBean.getOriginalWord(), newDBEntity), newLabel,
						newDBEntity, answerBean);
			} else {
				answerBean.setAnswer(answer.substring(0, answer.length() - 1));
				answerBean.setScore(score);
				// answerBean.setValid(true);
				System.out.println("\t EndOfRP  @@ return case 2, answer = " + answerBean);
				return answerBean.returnAnswer(answerBean);
			}
		}
	}

	private boolean hasPropertyInSentence(String sentence, String label, String entity) {
		List<String> candidateSet = this.getCandidateSet(sentence, entity);
		List<String> candidateSetbyStopWord = this.getCandidateSetbyStopWord(candidateSet);
		Map<String, String> propMap = this.getPropertyNameSet(label, entity);

		if (matchPropertyFromSentence(candidateSetbyStopWord, propMap).isEmpty())
			return false;
		else
			return true;
	}

	// to test if the user sentence is a question of relationship between two
	// entities
	private boolean isRelationshipQuestion(String sentence) {
		// TBD: hard code for 4/15
		if (sentence.contains("关系") || sentence.contains("联系"))
			return true;
		return false;
	}

	// to test if two list are equal
	private List<String> mergeTwoLists(List<String> lhs, List<String> rhs) {
		List<String> rsList = new ArrayList<>();
		rsList.addAll(rhs);
		for (String s : lhs) {
			if (!rhs.contains(s))
				rsList.add(s);
		}
		return rsList;
	}

	// to test if two list are equal
	private boolean isTwoListsEqual(List<String> lhs, List<String> rhs) {
		if (lhs.size() != rhs.size()) {
			return false;
		}
		for (String s : lhs) {
			if (!rhs.contains(s))
				return false;
		}
		return true;
	}

	// to test if the user want to get the introduction of the entity
	// input: 姚明是谁？ 你喜欢姚明吗？
	// output: 1 0
	private boolean isKindofQuestion(String sentence, String questionType, String entity) {
		boolean rs = false;
		if (Tool.isStrEmptyOrNull(sentence)) {
			return rs;
		}

		String template = "";
		if (!entity.isEmpty()) {
			if (!sentence.contains(entity)) {
				System.err.println("isKindofQuestion: sentence has no entity, s = " + sentence + ", e=" + entity);
				Debug.printDebug(uniqueID, 2, "KG", "isKindofQuestion: sentence has no entity");
				return false;
			}
			String first = sentence.substring(0, sentence.indexOf(entity));
			String second = sentence.substring(sentence.indexOf(entity) + entity.length(), sentence.length());
			System.out.println("isKindof: first=" + first + ", entity=" + entity + ", second=" + second);
			sentence = first + " ## " + entity + "<type>entity</type> " + second;
			template = questionClassifier.process(sentence);
		} else {
			template = questionClassifier.processQuestionClassifier(sentence);
		}

		if (!template.isEmpty() && template.startsWith(questionType)) {
			rs = true;
			System.out.println("~~~~ IS " + questionType);
		} else {
			System.out.println("template=" + template + "~~~~ NOT " + questionType);
		}
		return rs;
	}

	// implication question process
	private AnswerBean implicationQuestionProcess(String sentence, String entity, AnswerBean answerBean) {
		String strImplication = questionClassifier.processQuestionClassifier(sentence).replace(implicationQuestionType,
				"");
		System.out.println("implicationQuestionProcess str = " + strImplication);

		if (!answerBean.getAnswer().isEmpty()) {
			answerBean
					.setAnswer(ImplicationProcess.getImplicationAnswer(answerBean.getAnswer(), entity, strImplication));
		}
		System.out.println("Implication Qustion: anwerBean is " + answerBean.toString());
		return answerBean.returnAnswer(answerBean);
	}

	private AnswerBean selectiveQuestionProcess(String sentence, AnswerBean answerBean) {
		String strSeletive = questionClassifier.processQuestionClassifier(sentence).replace(selectiveQuestionType, "");
		System.out.println("selectiveQuestionProcess str = " + strSeletive);

		if (!answerBean.getAnswer().isEmpty()) {
			// valide answer
			answerBean.setAnswer(strSeletive.substring(0, 1) + "的, " + entitySet.get(0) + "的" + answerBean.getProperty()
					+ "是" + answerBean.getAnswer());
		} else {
			answerBean.setAnswer(strSeletive.substring(1));
		}
		System.out.println("Selective Qustion: anwerBean is " + answerBean.toString());
		return answerBean.returnAnswer(answerBean);
	}

	// Get the answer by the pattern matching method
	// for the case of single entity in multiple level reasoning case
	// input: the question sentence from users,"姚明的老婆的身高是多少"
	// output: the valid property contained in the sentence
	private List<PatternMatchingResultBean> matchPropertyFromSentence(List<String> candidateSet,
			Map<String, String> propMap) {
		System.out.println("\t matchPropertyFromSentence candidateSet=" + candidateSet);

		// 3. compute the score for each candidate
		List<PatternMatchingResultBean> rsBean = new ArrayList();
		System.out.println(
				"---------------Mutliple Entity: Begin of Pattern Matching Candidate Process--------------------");
		for (String candidate : candidateSet) {
			System.out.println("### Candidate is " + candidate);

			List<PatternMatchingResultBean> tempBeanSet = new ArrayList();

			// 2. generate candidates according to the synonyms
			List<String> synList = new ArrayList<>();
			Map<String, String> refPropMap = new HashMap<>();
			synList = this.replaceSynonymProcess(candidate, refPropMap);
			System.out.println("\t after synonym process: " + synList + " refPropMap=" + refPropMap);

			for (String syn : synList) {
				System.out.println("q=" + syn + " and s=" + candidate);
				int orignalScore = (syn.toLowerCase().equals(candidate.toLowerCase())) ? 100 : 80;
				PatternMatchingResultBean pmRB = new PatternMatchingResultBean();
				pmRB = this.recognizingProp(syn, propMap.keySet(), orignalScore);
				if (pmRB.isValid()) {
					tempBeanSet.add(pmRB);
					System.out.println("string " + syn + " has the answer of " + pmRB.getAnswer() + " with score "
							+ pmRB.getScore());
				}
			}

			// 4. verify and return the result with highest score
			double localFinalScore = Double.MIN_VALUE;
			String localPropName = "";
			for (PatternMatchingResultBean b : tempBeanSet) {
				if (b.isValid()) {
					System.out.println("candidate " + b.getAnswer() + " has score:" + b.getScore());
					if (b.getScore() > localFinalScore) {
						localPropName = b.getAnswer();
						localFinalScore = b.getScore();
					}
				}
			}

			if (!localPropName.isEmpty()) {
				PatternMatchingResultBean localrsBean = new PatternMatchingResultBean();
				localrsBean.setAnswer(propMap.get(localPropName));
				localrsBean.setScore(localFinalScore);
				String orignalWord = Tool.isStrEmptyOrNull(refPropMap.get(localPropName)) ? localPropName
						: refPropMap.get(localPropName);
				localrsBean.setOrignalWord(orignalWord);
				System.out.println("\t\t localPropName=" + localPropName + ", propMapName=" + localrsBean.getAnswer()
						+ ", originalName=" + localrsBean.getOrignalWord());
				rsBean.add(localrsBean);
			}
		}
		System.out.println(
				"---------------Multiple Entity: End of Pattern Matching Candidate Process--------------------");

		System.out.println("PM.getSingleEntityNormalQ return bean is " + rsBean.toString());
		return rsBean;
	}

	// get the relationship set in DB with synonym process
	// return Map<synRelation, relationship>
	// input: 姚明
	// output: [<位置,位置>, <妻,老婆>, ...]
	private Map<String, String> getRelationshipSet(String label, String ent) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getRelationshipSet: input is empty");
			return rsMap;
		}

		List<String> rList = DBProcess.getRelationshipSet(label, ent);
		for (String iRelation : rList) {
			rsMap.put(iRelation, iRelation);
			Set<String> setSyn = NLPProcess.getSynonymWordSet(iRelation);
			for (String iSyn : setSyn) {
				rsMap.put(iSyn, iRelation);
			}
		}
		System.out.println("all the relationhip of " + ent + "is: " + rsMap);
		return rsMap;
	}

	// get the property set in DB with synonym process
	// return Map<synProp, prop>
	// input: 姚明
	// output: [<老婆,老婆>, <妻,老婆>, ...]
	private Map<String, String> getPropertyNameSet(String label, String ent) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getPropertyNameSet: input is empty");
			return rsMap;
		}

		List<String> propList = DBProcess.getPropertyNameSet(label, ent);
		if (propList != null && !propList.isEmpty()) {
			for (String iProp : propList) {
				rsMap.put(iProp, iProp);
				Set<String> setSyn = NLPProcess.getSynonymWordSet(iProp);
				for (String iSyn : setSyn) {
					rsMap.put(iSyn, iProp);
				}
			}
		}
		// System.out.println("all the prop is: " + rsMap);
		return rsMap;
	}

	// return the entity by Solr method
	// input: the sentence from user, "姚明身高多少"
	// output: the entity identified by Solr, "姚明"
	private List<String> getEntityBySolr(String sentence, List<String> entitySet, List<String> segWord) {
		System.out.println("getEntityBySolr: segWord=" + segWord);
		List<String> rsEntitySet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getEntityBySolr: input is empty");
			return rsEntitySet;
		}

		SolrUtil solr = new SolrUtil();
		Solr_Query obj = new Solr_Query();

		if (entitySet != null && !entitySet.isEmpty()) {
			obj.setFindEntity(true);
			obj.setEntity(entitySet);
		}

		for (String s : segWord) {
			obj.addWord(s);

			// if (!NLPProcess.isInHighFreqDict(s)) {
			// obj.addWord(s);
			// }

		}

		rsEntitySet = solr.Search(obj);
		System.out.println("getEntityBySolr return: " + rsEntitySet);
		return rsEntitySet;
	}

	// get the candidiates by spliting the sentence accroding to the entity
	// input: [你知道，的老婆的身高吗]（“你知道姚明的老婆的身高吗？”）
	// output: [老婆，身高]
	// if question does not contain ent, return null.
	private List<String> getCandidateSetbyStopWord(List<String> strSet) {
		List<String> rsList = new ArrayList<>();
		if (strSet == null) {
			System.err.println("PMP.getCandidateSet: input is empty");
			Debug.printDebug(uniqueID, 2, "knowledge", "PMP.getCandidateSetbyStopWord: input is empty");
		}

		// System.err.println("PMP.getCandidateSetbyStopWord: input="+strSet);
		for (String str : strSet) {
			if (CharUtil.trim(str).isEmpty()) {
				continue;
			}
			// remove below for fixing case : "7号房的礼物是啥类型的电影"
			// rsList.add(str);

			String littleCandidate = "";
			List<Term> segPos = NLPProcess.getSegWord(str);
			System.out.println("PMP.getCandidateSetbyStopWord: segPos=" + segPos);

			for (int i = 0; i < segPos.size(); i++) {
				String segWord = segPos.get(i).word;
				if (!NLPProcess.isStopWord(segWord)) {
					// not stopword
					littleCandidate += segWord;
					// System.err.println("NotStopWord: segWord="+segWord+",
					// little=" + littleCandidate);
				} else {
					if (!littleCandidate.isEmpty()) {
						rsList.add(littleCandidate);
						// System.err.println("StopWord 1: segWord="+segWord+",
						// little=" + littleCandidate);
						littleCandidate = "";
					} else {
						// System.err.println("StopWord 2: segWord="+segWord+",
						// little=" + littleCandidate);
					}
				}
			}
			// remove below for fixing case : "7号房的礼物是啥类型的电影"
			// move the case of empty to getCandidateSetbyEntity process
			if (!littleCandidate.isEmpty()) {
				rsList.add(littleCandidate);
			}
		}
		System.out.println("\t getCandidateSetbyEntityandStopWord is " + rsList.toString());
		return rsList;
	}

	// get the candidiates by spliting the sentence accroding to the entity
	// input: “你知道姚明的身高吗？”
	// output: [“你知道”，“的身高吗”]
	// if question does not contain ent, return null.
	private List<String> getCandidateSet(String str, String ent) {
		List<String> listPart = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(str) || Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getCandidateSet: input is empty");
		}

		while (str.lastIndexOf(ent) != -1) {
			String s = CharUtil.trim(str.substring(str.lastIndexOf(ent) + ent.length()));
			if (!s.isEmpty()) {
				// System.out.println("PMP.GetCandidateSet, s ="+s);
				listPart.add(s); // add the last part
			}

			// remove the last part
			str = CharUtil.trim(str.substring(0, str.lastIndexOf(ent)));
			if (!str.isEmpty() && str.lastIndexOf(ent) == -1) {
				listPart.add(str);
				// System.out.println("PMP.GetCandidateSet: str=" + str);
			}
		}
		System.out.println("\t getCandidateSet=" + listPart);
		return listPart;
	}

	// get the candidiates by spliting the sentence accroding to the entity and
	// return the words after removing stop word
	// input: “斗罗大陆属于哪种小说”
	// output: [属于，哪种，小说]
	// if question does not contain ent, return null.
	private List<String> getCandidateSetbyNLP(List<String> strList) {
		List<String> rsList = new ArrayList<>();
		if (strList == null) {
			System.err.println("PMP.getCandidateSetbyNLP: input is empty");
			Debug.printDebug(uniqueID, 2, "knowledge", "null in getCandidateSetbyNLP");
			return rsList;
		}

		for (String str : strList) {
			List<Term> segPos = NLPProcess.getSegWord(str);
			for (int i = 0; i < segPos.size(); i++) {
				String segWord = CharUtil.trim(segPos.get(i).word);
				if (!segWord.isEmpty()) {
					rsList.add(segWord);
				}
			}
		}
		System.out.println("\t getCandidateSetbyNLP=" + rsList);
		Debug.printDebug(uniqueID, 4, "knowledge", "\t getCandidateSetbyNLP=" + rsList);
		return rsList;
	}

	// generate all the possibility candidates according to synonyms
	// input: 这个标志多少
	// output: [这个记号数量, 这个标志数量, 这个记号多少, 这个标志多少]
	private List<String> replaceSynonymProcess(String str, Map<String, String> refMap) {
		// System.out.println("input of replaceSynonymProcess is " + str);
		List<String> rsSet = new ArrayList<>();
		if (str.isEmpty()) {
			System.out.println("output of replaceSynonymProcess is " + rsSet);
			return rsSet;
		}

		// NLPResult tnNode = NLPSevice.ProcessSentence(str,
		// NLPFlag.SegPos.getValue());
		// List<Term> segPos = tnNode.getWordPos();
		List<Term> segPos = NLPProcess.getSegWord(str);
		rsSet.add("");
		for (int i = 0; i < segPos.size(); i++) {
			String iWord = segPos.get(i).word;
			// System.out.println("current word is " + iWord);

			Set<String> iSynSet = NLPProcess.getSynonymWordSet(iWord);
			if (!iSynSet.contains(iWord)) {
				iSynSet.add(iWord);
				refMap.put(iWord, iWord);
			}
			if (iSynSet.size() > 0) {
				// System.out.println(iWord + " has syn: " + iSynSet);
				// combine each of the synonyms to generate mutliple candidates
				List<String> newRS = new ArrayList<>();
				for (String iSyn : iSynSet) {
					refMap.put(iSyn, iWord);
					// System.out.println("\t iSyn is " + iSyn);
					List<String> tmpRS = new ArrayList<>();
					tmpRS.addAll(rsSet);
					for (int j = 0; j < tmpRS.size(); j++) {
						tmpRS.set(j, tmpRS.get(j) + iSyn);
					}
					newRS.addAll(tmpRS);
					// System.out.println("\t tempRS is: " + tmpRS + "; newRS is
					// " + newRS);
				}
				rsSet = newRS;
				// System.out.println("after syn is: " + newRS);
			} else {
				System.err.println("PMP.replaceSynonym: No syn: " + iWord);
				for (int j = 0; j < rsSet.size(); j++) {
					rsSet.set(j, rsSet.get(j) + iWord);
				}
			}
		}

		// System.out.println("output of replaceSynonym: " + rsSet.toString());
		return rsSet;
	}

	private boolean isMapEqual(HashMap<String, Integer> lhs, HashMap<String, Integer> rhs) {

		for (String s : lhs.keySet()) {
			if (!rhs.containsKey(s)) {
				System.err.print(">>>isMapEqual: rhs does not contain " + s);
				return false;
			} else {
				if (lhs.get(s) != rhs.get(s)) {
					System.err.print(">>>isMapEqual: lhs.get(s)=" + lhs.get(s) + ", rhs.get(s)=" + rhs.get(s));
					return false;
				}
			}
		}

		return true;
	}

	// test the similarity between target (strProperty) and ref (candidate)
	private boolean SinglePatternMatching(HashMap<String, Integer> rsMap, String strProperty, String candidate,
			boolean isPass) {
		// System.out.println(">>>SinglePatternMatching: rsMap = " + rsMap +
		// "\t"+"strProperty=" + strProperty
		// + ", candidate=" + candidate);

		// case of length == 1
		if (strProperty.length() == 1 || candidate.length() == 1) {
			if (strProperty.equals(candidate)) {
				rsMap.put(strProperty, 5);
				isPass = true;
			} else {
				rsMap.put(strProperty, Integer.MIN_VALUE);
			}
			return isPass;
		}

		// case of length == 2
		if (strProperty.length() == 2 || candidate.length() == 2) {
			String longStr = (strProperty.length() > candidate.length()) ? strProperty : candidate;
			String shortStr = (strProperty.length() > candidate.length()) ? candidate : strProperty;
			if (longStr.contains(shortStr) && longStr.length() <= shortStr.length() * 2) {
				int iScore = (strProperty.equals(candidate)) ? 5 : 5 * shortStr.length() / longStr.length();
				rsMap.put(strProperty, iScore);
				isPass = true;
			} else {
				rsMap.put(strProperty, Integer.MIN_VALUE);
			}
			return isPass;
		}

		if (!isPass && candidate.lastIndexOf(strProperty) != -1) {
			isPass = true;
		}

		// pattern matching algorithm suggested by Phantom
		// compute the score by scanning the sentence from left to right
		// compare each char, if match, socre++, else score--
		String tmpProp = strProperty.toLowerCase();
		int left2right = 0;
		for (int i = 0; i < candidate.length(); i++) {
			if (tmpProp.indexOf(candidate.charAt(i)) == 0) {
				left2right++;
				tmpProp = tmpProp.substring(1);
			} else {

				left2right--;
			}
			// System.out.println("candidate at i = " +candidate.charAt(i) + ",
			// left2right = " + left2right);
		}
		if (tmpProp.isEmpty()) {
			isPass = true;
		}
		// System.out.println("left is " + left2right);

		// case: "sentence is 所属运动队, prop is 运动项目; left=-1, right=-5"
		// extend the algorithm by adding the process from right to left
		// compute the score by scanning from right to left
		tmpProp = strProperty.toLowerCase();
		int right2left = 0;
		for (int i = candidate.length() - 1; i >= 0; i--) {
			// System.out.println(tmpProp + " " + str.charAt(i));
			if (!tmpProp.isEmpty() && tmpProp.lastIndexOf(candidate.charAt(i)) == tmpProp.length() - 1) {
				right2left++;
				tmpProp = tmpProp.substring(0, tmpProp.length() - 1);
			} else {
				right2left--;
			}
		}
		// System.out.println("right is " + right2left + " isPass is " +
		// isPass);

		// if (left2right != right2left)
		// System.err.println(
		// "sentence is " + str + ", prop is " + s + "; left=" + left2right
		// + ", right=" + right2left);

		if (left2right > right2left) {
			rsMap.put(strProperty, left2right);
		} else {
			rsMap.put(strProperty, right2left);
		}

		return isPass;
	}

	// return the property with the highest score; return null if the threshold
	// is hold the version without segPos
	// input: （姚明）妻
	// output: 叶莉
	private PatternMatchingResultBean getCandidatePropName(String candidate, List<String> propSet, int originalScore) {
		// threshold to pass: if str contain a property in DB, pass
		boolean isPass = false;
		HashMap<String, Integer> rsMap = new HashMap<String, Integer>();
		PatternMatchingResultBean beanPM = new PatternMatchingResultBean();

		if (Tool.isStrEmptyOrNull(candidate) || propSet == null) {
			System.err.println("PMP.getCandidatePropName: input is empty");
			return beanPM;
		}
		candidate = candidate.toLowerCase();
		System.out.println("query string is: " + candidate);

		HashMap<String, Integer> rsMapRef = new HashMap<String, Integer>();
		boolean isPassRef = false;

		for (String strProperty : propSet) {
			// System.out.println("current prop is: " + s);

			isPassRef = SinglePatternMatching(rsMapRef, strProperty, candidate, isPass);

			if (!isPass && candidate.lastIndexOf(strProperty) != -1) {
				isPass = true;
			}

			// pattern matching algorithm suggested by Phantom
			// compute the score by scanning the sentence from left to right
			// compare each char, if match, socre++, else score--
			String tmpProp = strProperty.toLowerCase();
			int left2right = 0;
			for (int i = 0; i < candidate.length(); i++) {
				if (tmpProp.indexOf(candidate.charAt(i)) == 0) {
					left2right++;
					tmpProp = tmpProp.substring(1);
				} else {

					left2right--;
				}
			}
			if (tmpProp.isEmpty()) {
				isPass = true;
			}
			// System.out.println("left is " + left2right);

			// case: "sentence is 所属运动队, prop is 运动项目; left=-1, right=-5"
			// extend the algorithm by adding the process from right to left
			// compute the score by scanning from right to left
			tmpProp = strProperty.toLowerCase();
			int right2left = 0;
			for (int i = candidate.length() - 1; i >= 0; i--) {
				// System.out.println(tmpProp + " " + str.charAt(i));
				if (!tmpProp.isEmpty() && tmpProp.lastIndexOf(candidate.charAt(i)) == tmpProp.length() - 1) {
					right2left++;
					tmpProp = tmpProp.substring(0, tmpProp.length() - 1);
				} else {
					right2left--;
				}
			}
			// System.out.println("right is " + right2left + " isPass is " +
			// isPass);

			// if (left2right != right2left)
			// System.err.println(
			// "sentence is " + str + ", prop is " + s + "; left=" + left2right
			// + ", right=" + right2left);

			if (left2right > right2left) {
				rsMap.put(strProperty, left2right);
			} else {
				rsMap.put(strProperty, right2left);
			}

			if (isPass != isPassRef) {
				System.err.println("isPass error");
			}
		}

		if (!isMapEqual(rsMap, rsMapRef)) {
			System.err.println("rsMap error");
		}

		int finalScore = Integer.MIN_VALUE;
		for (String s : propSet) {
			if (rsMap.get(s) > finalScore) {
				finalScore = rsMap.get(s);
				beanPM.setAnswer(s);
				beanPM.setScore(finalScore);
			}
		}
		if (isPass == false && beanPM.getScore() < 0) {
			beanPM.set2NotValid();
		} else {
			System.out.println("original score is " + originalScore + " and final score is: " + beanPM.getScore());
			if (isPass == true && candidate.lastIndexOf(beanPM.getAnswer()) == -1) {
				System.err.println("threshold pass but the candidate does not contain the property");
			}
			double fs = (isPass == false) ? originalScore * (beanPM.getScore() * 0.1 + 0.5) : originalScore;
			beanPM.setScore(fs);
		}
		System.out.println("in GetPropName---finalScore is " + finalScore + ". rs is " + beanPM.toString());
		return beanPM;
	}

	// return the property with the highest score; return null if the threshold
	// is hold the version without segPos
	// input: （姚明）妻
	// output: 叶莉
	private PatternMatchingResultBean recognizingProp(String candidate, Set<String> propSet, int originalScore) {
		System.out.println("init of recognizingProp: candidate=" + candidate);
		// threshold to pass: if str contain a property in DB, pass
		boolean isPass = false;
		HashMap<String, Integer> rsMap = new HashMap<String, Integer>();
		PatternMatchingResultBean beanPM = new PatternMatchingResultBean();

		if (Tool.isStrEmptyOrNull(candidate) || propSet == null) {
			System.err.println("PMP.recognizingProp: input is empty");
			return beanPM;
		}
		candidate = candidate.toLowerCase();

		for (String strProperty : propSet) {
			isPass = SinglePatternMatching(rsMap, strProperty, candidate, isPass);
		}

		int finalScore = Integer.MIN_VALUE;
		for (String s : propSet) {
			if (rsMap.get(s) > finalScore) {
				finalScore = rsMap.get(s);
				beanPM.setAnswer(s);
				beanPM.setScore(finalScore);
			}
		}
		if (isPass == false && beanPM.getScore() < 0) {
			beanPM.set2NotValid();
		} else {
			System.out.println("original score is " + originalScore + " and final score is: " + beanPM.getScore());
			if (isPass == true && candidate.lastIndexOf(beanPM.getAnswer()) == -1) {
				System.err.println("threshold pass but the candidate does not contain the property");
			}
			double fs = (isPass == false) ? originalScore * (beanPM.getScore() * 0.1 + 0.5) : originalScore;
			beanPM.setScore(fs);
		}
		System.out.println("in recognizingProp---finalScore is " + finalScore + ". rs is " + beanPM.toString());
		return beanPM;
	}

	public static void main(String[] args) {
		NLPProcess nlpProcess = new NLPProcess();
		NLPProcess.NLPProcessInit();
		String str = "然并卵";
		CUBean bean = new CUBean();
		bean.setText(str);
		bean.setQuestionType("question");
		bean.setScore("50");
		PatternMatchingProcess mp = new PatternMatchingProcess(bean);
		mp.getAnswer();
		// System.out.println("template=" + mp.templateProcess("姚明", str));

	}

}
