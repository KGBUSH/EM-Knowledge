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

import com.emotibot.WebService.AnswerBean;
import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.emotibot.solr.SolrUtil;
import com.emotibot.solr.Solr_Query;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class PatternMatchingProcess {

	final TemplateProcessor sentenceTemplate = new TemplateProcessor("Knowledge");
	final TemplateProcessor selectiveQuestionTemplate = new TemplateProcessor("Pre");
	final TemplateProcessor introductionTemplate = new TemplateProcessor("Post");

	// The entrance to understand the user query and get answer from Neo4j
	// input: the question sentence from users,"姚明身高是多少"
	// output: the answer without answer rewriting, “226cm”
	public AnswerBean getAnswer(String userSentence) {

		String sentence = userSentence;
		// System.out.println("PMP.getAnswer: sentence = " + sentence);
		AnswerBean answerBean = new AnswerBean();
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getAnswer: input is empty");
			return answerBean;
		}

		boolean isQuestion = true; // TBD: read from CU

		// 1. get the entity by Solr and Revise by template
		List<String> entitySet = getEntity(sentence);
		if (entitySet.size() > 2) {
			// check for 4/15 temporarily, may extent later
			System.err.println("PM.getAnswer: there are more than two entities");
		}

		// TBD: if the sentence does not contain the entity, go through the
		// process of matching value of properties
		System.out.println("PMP.getAnswer: entity = " + entitySet.toString());
		if (entitySet == null || entitySet.isEmpty()) {
			System.out.println("the sentence does not contain entity name and so return empty");
			return answerBean;
		}

		// PatternMatchingResultBean beanPM = new PatternMatchingResultBean();
		String entity = "";
		// for single entity case
		// if (entitySet.size() == 1) {
		if (entitySet.size() > 0) { // TBD: add case for size > 1
			entity = entitySet.get(0);
			sentence = templateProcess(entity, sentence);
			System.out.println("PMP.getAnswer: single entity templateProcess sentence = " + sentence);
			if (Tool.isStrEmptyOrNull(sentence)) {
				System.err.println("the sentence become empty after template process");
				return answerBean;
			}

			// answerBean = mutlipleReasoningProcess(sentence, entity);
			answerBean = ReasoningProcess(sentence, entity, answerBean);

			// old method for handling single property
			// beanPM = getSingleEntityNormalQ(userSentence, entity);

			// List<PatternMatchingResultBean> listPMBean =
			// this.matchPropertyFromSentence(sentence, entity);
			// answerBean = mutlipleReasoningProcess(entity, listPMBean);
		} else {
			// for multiple entities, support at most two in 4/15 milestone

		}

		// if it is the selective question
		String strSeletive = processSelectQ(userSentence);
		if (!strSeletive.isEmpty()) {
			if (!answerBean.getAnswer().isEmpty()) {
				// valide answer
				answerBean.setAnswer(strSeletive.substring(0, 1) + answerBean.getAnswer());
			} else {
				answerBean.setAnswer(strSeletive.substring(1));
			}
			System.out.println("Selective Qustion: anwerBean is " + answerBean.toString());
			return answerBean;
		}

		if (!answerBean.getAnswer().isEmpty()) {
			// case of not matching property
			if (isQuestion == false) {
				answerBean.setScore(0);
			}
		} else {
			// case of matching property
			answerBean.setAnswer(DBProcess.getPropertyValue(entity, "firtParamInfo"));
			answerBean.setScore(isIntroduction(userSentence) ? 100 : 0);
		}
		System.out.println("PM.getAnswer: the returned anwer is " + answerBean.toString());
		return answerBean;
	}

	private List<PatternMatchingResultBean> copyPMListExceptOne(List<PatternMatchingResultBean> listPMBean,
			PatternMatchingResultBean one) {
		List<PatternMatchingResultBean> copy = new ArrayList<PatternMatchingResultBean>(listPMBean.size());
		Iterator<PatternMatchingResultBean> it = listPMBean.iterator();
		while (it.hasNext()) {
			PatternMatchingResultBean b = it.next();
			if (!b.equals(one)) {
				copy.add(b.clone());
			}
		}
		return copy;
	}

	// to handle the case that not all the property will be used
	// input: "姚明", ["身高"，"老婆", "祖籍"]
	// output: “190”
	private String mutlipleReasoningProcessException(String entity, List<PatternMatchingResultBean> listPMBean) {
		System.out.println("MLProcess: entity=" + entity + "; listPMBean=" + listPMBean);

		String answer = coreMutlipleReasoningProcess(entity, listPMBean);
		if (!answer.isEmpty()) {
			return answer;
		}

		String rs = "";
		for (PatternMatchingResultBean bean : listPMBean) {
			List<PatternMatchingResultBean> beanList = copyPMListExceptOne(listPMBean, bean);
			String localRS = mutlipleReasoningProcessException(entity, beanList);
			if (!Tool.isStrEmptyOrNull(localRS)) {
				return localRS;
			}
		}

		return rs;
	}

	private AnswerBean mutlipleReasoningProcess(String sentence, String entity) {
		System.out.println("PMP.mutlipleReasoningProcess: entity=" + entity + ", sentence =" + sentence);

		List<PatternMatchingResultBean> listPMBean = this.matchPropertyFromSentence(sentence, entity);
		System.out.println("PMP.mutlipleReasoningProcess: listPMBean=" + listPMBean);

		AnswerBean rsBean = new AnswerBean();
		String answer = coreMutlipleReasoningProcess(entity, listPMBean);

		if (answer.isEmpty()) {
			return rsBean;
		} else {
			rsBean.setAnswer(answer);

			double score = 100;
			for (PatternMatchingResultBean bean : listPMBean) {
				score *= bean.getScore() / 100.00;
				System.out.print(" * " + bean.getScore() + "/100 ");
			}
			System.out.println(" = " + score);
			rsBean.setScore(score);
		}

		return rsBean;
	}

	// interface
	private AnswerBean ReasoningProcess(String sentence, String entity, AnswerBean answerBean) {
		System.out.println(
				"PMP.ReasoningProcess: sentence=" + sentence + ", entity =" + entity + ", bean is " + answerBean);

		if (!sentence.contains(entity)) {
			System.err.println("Sentence does not contain entity");
			return answerBean;
		}
		String newSentence = sentence.substring(0, sentence.indexOf(entity))
				+ sentence.substring(sentence.indexOf(entity) + entity.length());
		System.out.println("\t new sentence is::::" + newSentence);

		Map<String, String> relationMap = this.getRelationshipSet(entity);
		System.out.println("\t relationMap = " + relationMap);

		List<PatternMatchingResultBean> listPMBean = this.matchPropertyFromSentence(sentence, entity);
		System.out.println("\t listPMBean=" + listPMBean);

		if (listPMBean.isEmpty()) {
			System.out.println("\t @@ return case 0, answer=" + answerBean);
			answerBean.setScore(answerBean.getScore() / 2); // does not match
															// property, score
															// decreases
			return answerBean;
		} else if (listPMBean.size() == 1) {
			String prop = listPMBean.get(0).getAnswer();
			String answer = DBProcess.getPropertyValue(entity, prop);
			answerBean.setAnswer(answer);
			answerBean.setProperty(prop);
			answerBean.setValid(true);
			answerBean.setScore(listPMBean.get(0).getScore());
			String oldWord = listPMBean.get(0).getOrignalWord();
			answerBean.setOriginalWord(oldWord);
			System.out.println("\t answer = " + answerBean);

			if (relationMap.containsKey(prop)) {
				// use the new answer as new entity
				String newDBEntity = DBProcess.getEntityByRelationship("", entity, prop);
				System.out.println("-----> case 1 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				System.out.println("prop:" + prop + ", new sentence:" + newSentence + ", after replace:"
						+ newSentence.replace(oldWord, newDBEntity));
				return ReasoningProcess(newSentence.replace(oldWord, newDBEntity), newDBEntity, answerBean);
			} else {
				System.out.println("\t @@ return case 1, answer=" + answerBean);
				return answerBean;
			}
		} else {
			// in the case of multiple props, find a way out
			String answer = "";
			double score = 100;

			boolean furtherSeach = false;
			String prop = "";

			for (PatternMatchingResultBean b : listPMBean) {
				String queryAnswer = DBProcess.getPropertyValue(entity, b.getAnswer());
				if (relationMap.containsKey(b.getAnswer())) {
					furtherSeach = true;
					prop = b.getAnswer();
					answerBean.setAnswer(queryAnswer);
					answerBean.setProperty(b.getAnswer());
					answerBean.setValid(true);
					answerBean.setScore(b.getScore());
					String oldWord = b.getOrignalWord();
					answerBean.setOriginalWord(oldWord);
					break;
				} else {
					answer += queryAnswer;
					score *= b.getScore() / 100;
					System.out.print(" * " + b.getScore() + "/100 ");
				}
			}

			if (furtherSeach == true) {
				String newDBEntity = DBProcess.getEntityByRelationship("", entity, prop);
				System.out.println("-----> case 2 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				return ReasoningProcess(newSentence.replace(answerBean.getOriginalWord(), newDBEntity), newDBEntity,
						answerBean);
			} else {
				answerBean.setAnswer(answer);
				answerBean.setScore(score);
				answerBean.setValid(true);
				System.out.println("\t @@ return case 2, answer = " + answerBean);
				return answerBean;
			}
		}
	}

	private AnswerBean getNextLevelRS(String sentence, String entity, String property) {
		AnswerBean rsBean = new AnswerBean();

		// get all the relationship of the entity
		Map<String, String> relationMap = this.getRelationshipSet(entity);
		System.out.println("PMP.getAnswer: relationMap = " + relationMap);

		if (!relationMap.containsKey(property)) {
			String rs = DBProcess.getPropertyValue(entity, property);
		} else {
			String newSentence = sentence.substring(0, sentence.indexOf(property))
					+ sentence.substring(sentence.indexOf(property) + property.length());
			System.out.println("new sentence is::::" + newSentence);
			return mutlipleReasoningProcess(newSentence, entity);
		}

		return rsBean;

	}

	// get the anwer by multi-level reasoning
	// input: "姚明", ["身高"，"老婆"]
	// output: “190”
	private String coreMutlipleReasoningProcess(String entity, List<PatternMatchingResultBean> listPMBean) {
		System.out.println("coreMLProcess: entity=" + entity + "; listPMBean=" + listPMBean);
		String rs = "";
		if (listPMBean.size() == 1) {
			rs = DBProcess.getPropertyValue(entity, listPMBean.get(0).getAnswer());
			return rs;
			// rs = getAnswerbyProperty(entity, listPMBean.get(0).getAnswer());
		}

		// get all the relationship of the entity
		Map<String, String> relationMap = this.getRelationshipSet(entity);
		System.out.println("PMP.getAnswer: relationMap = " + relationMap);

		Iterator<PatternMatchingResultBean> iterator = listPMBean.iterator();
		while (iterator.hasNext()) {
			PatternMatchingResultBean bean = iterator.next();
			// System.out.println("$$$$check:"+bean);
			if (relationMap.containsKey(bean.getAnswer())) {
				System.out.println("@@@process" + bean);
				List<PatternMatchingResultBean> copy = copyPMListExceptOne(listPMBean, bean);
				String nextEntity = getEntitybyRelation(entity, bean.getAnswer());
				System.out.println("copy=" + copy + ", newEntity=" + nextEntity);
				if (!nextEntity.isEmpty()) {
					System.out.println("-----> recurrence into: nextEntity=" + nextEntity);
					rs = coreMutlipleReasoningProcess(nextEntity, copy);

				}
			} else {
				System.out.println("@@@remove" + bean);
				iterator.remove();
			}
		}

		return rs;

	}

	private String getAnswerbyProperty(String entity, String property) {
		if (Tool.isStrEmptyOrNull(entity) || Tool.isStrEmptyOrNull(property)) {
			System.err.println("entity or property is null");
			return "";
		}
		System.out.println("return get anwer by property");
		String rsProp = "";

		if (entity.equals("叶莉")) {
			if (property.equals("身高")) {
				return "190";
			}
		}

		return rsProp;
	}

	private String getEntitybyRelation(String entity, String relation) {
		String rsEntity = "";
		// TBD: get the entity from DB by entity and relation

		// Debug
		if (entity.equals("姚明")) {
			if (relation.equals("老婆")) {
				return "叶莉";
			}
		}

		return rsEntity;
	}

	private List<String> getEntity(String sentence) {
		System.out.println("PMP.getEntity: sentence=" + sentence);
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getEntity: input is empty");
			return null;
		}

		List<String> rsEntity = new ArrayList<>();
		List<String> simpleMatchEntity = NLPProcess.getEntitySimpleMatch(sentence);
		List<String> solrEntity = new ArrayList<>();

		if (simpleMatchEntity.isEmpty()) {
			solrEntity = getEntityBySolr(sentence, false);
			rsEntity = solrEntity;
		} else {
			solrEntity = getEntityBySolr(sentence, true);

			// get the intersection of two sets
			for (String s : simpleMatchEntity) {
				if (solrEntity.contains(s)) {
					rsEntity.add(s);
				}
			}
		}

		System.out.println(
				"\t simpleEntity = " + simpleMatchEntity + ",\n solrEntity=" + solrEntity + ",\n rsEntity=" + rsEntity);
		return rsEntity;
	}

	// to test if the user want to get the introduction of the entity
	// input: 姚明是谁？ 你喜欢姚明吗？
	// output: 1 0
	private boolean isIntroduction(String sentence) {
		boolean rs = false;
		if (Tool.isStrEmptyOrNull(sentence)) {
			return rs;
		}

		String feedback = introductionTemplate.process(sentence);
		if (!feedback.isEmpty()) {
			rs = true;
			System.out.println("isIntro: input:" + sentence + " is to ask the introduction");
		} else {
			System.out.println("isIntro: input:" + sentence + " is NOT ");
		}
		return rs;
	}

	// to test if the user question is a seletive question or not
	// input: 姚明有老婆吗？
	// output: 有没有
	private String processSelectQ(String sentence) {
		if (Tool.isStrEmptyOrNull(sentence)) {
			return "";
		}

		String rs = selectiveQuestionTemplate.process(sentence);
		if (!rs.isEmpty()) {
			System.out.println("isSelectQ: input=" + sentence + ", output=" + rs);
		}
		System.out.println("isSelectQ: input=" + sentence + ", output=" + rs);
		return rs;
	}

	// Get the answer by the pattern matching method
	// for the case of single entity in multiple level reasoning case
	// input: the question sentence from users,"姚明的老婆的身高是多少"
	// output: the answer without answer rewriting, “190cm”
	private List<PatternMatchingResultBean> matchPropertyFromSentence(String sentence, String entity) {
		// 2. split the sentence by the entities to get candidates
		// get candidates by splitting the sentence by entities.
		List<String> candidateSet = this.getCandidateSetbyStopWord(this.getCandidateSet(sentence, entity));
		System.out.println("PMP.getAnswer: candidateSet = " + candidateSet);

		// get all the property of the entity
		Map<String, String> propMap = this.getPropertyNameSet(entity);
		System.out.println("PMP.getAnswer: propMap = " + propMap);

		// 3. compute the score for each candidate
		List<PatternMatchingResultBean> rsBean = new ArrayList();
		System.out.println(
				"---------------Mutliple Entity: Begin of Pattern Matching Candidate Process--------------------");
		for (String s : candidateSet) {
			System.out.println("### Candidate is " + s);

			List<PatternMatchingResultBean> tempBeanSet = new ArrayList();

			// 2. generate candidates according to the synonyms
			List<String> synList = new ArrayList<>();
			Map<String, String> refPropMap = new HashMap<>();
			synList = this.replaceSynonymProcess(s, refPropMap);
			System.out.println("\t after synonym process: " + synList + " & size is " + synList.size());

			for (String q : synList) {
				System.out.println("q=" + q + " and s=" + s);
				int orignalScore = (q.equals(s)) ? 100 : 80;
				PatternMatchingResultBean pmRB = this.getCandidatePropName(q, propMap, orignalScore);
				if (pmRB.isValid()) {
					tempBeanSet.add(pmRB);
					System.out.println("string " + q + " has the answer of " + pmRB.getAnswer() + " with score "
							+ pmRB.getScore());
				}
			}

			// 4. Build the Cypher SQL and get the answer
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
				localrsBean.setOrignalWord(refPropMap.get(localPropName));
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

	// Get the answer by the pattern matching method
	// for the case of single entity in normal question mode
	// input: the question sentence from users,"姚明身高是多少"
	// output: the answer without answer rewriting, “226cm”
	private PatternMatchingResultBean getSingleEntityNormalQ(String sentence, String entity) {
		// 2. split the sentence by the entities to get candidates
		// get candidates by splitting the sentence by entities.
		List<String> candidateSet = this.getCandidateSet(sentence, entity);
		System.out.println("PMP.getAnswer: candidateSet = " + candidateSet);

		// get all the property of the entity
		Map<String, String> propMap = this.getPropertyNameSet(entity);
		System.out.println("PMP.getAnswer: propMap = " + propMap);

		// 3. compute the score for each candidate
		List<PatternMatchingResultBean> rsBean = new ArrayList();
		System.out.println(
				"---------------Single Entity: Begin of Pattern Matching Candidate Process--------------------");
		for (String s : candidateSet) {
			System.out.println("### Candidate is " + s);

			// 1. remove the stop words like "多少" in a sentence
			s = this.removeStopWord(s);
			System.out.println("\t after removing Stop Words: " + s);

			// 2. generate candidates according to the synonyms
			List<String> synList = new ArrayList<>();
			Map<String, String> refPropMap = new HashMap<>();
			synList = this.replaceSynonymProcess(s, refPropMap);
			System.out.println("\t after synonym process: " + synList + " & size is " + synList.size());

			for (String q : synList) {
				System.out.println("q=" + q + " and s=" + s);
				int orignalScore = (q.equals(s)) ? 100 : 80;
				PatternMatchingResultBean pmRB = this.getCandidatePropName(q, propMap, orignalScore);
				if (pmRB.isValid()) {
					rsBean.add(pmRB);
					System.out.println("string " + q + " has the answer of " + pmRB.getAnswer() + " with score "
							+ pmRB.getScore());
				}
			}
		}
		System.out
				.println("---------------Single Entity: End of Pattern Matching Candidate Process--------------------");

		// 4. Build the Cypher SQL and get the answer
		double finalScore = Double.MIN_VALUE;
		String propName = "";
		for (PatternMatchingResultBean b : rsBean) {
			if (b.isValid()) {
				System.out.println("candidate " + b.getAnswer() + " has score:" + b.getScore());
				if (b.getScore() > finalScore) {
					propName = b.getAnswer();
					finalScore = b.getScore();
				}
			}
		}

		PatternMatchingResultBean rs = new PatternMatchingResultBean();
		if (!propName.isEmpty()) {
			rs.setAnswer(propMap.get(propName));
			rs.setScore(finalScore);
		}

		System.out.println("PM.getSingleEntityNormalQ return bean is " + rs.toString());
		return rs;
	}

	// get the relationship set in DB with synonym process
	// return Map<synRelation, relationship>
	// input: 姚明
	// output: [<位置,位置>, <妻,老婆>, ...]
	private Map<String, String> getRelationshipSet(String ent) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getRelationshipSet: input is empty");
			return rsMap;
		}

		List<String> rList = DBProcess.getRelationshipSet(ent);
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
	private Map<String, String> getPropertyNameSet(String ent) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getPropertyNameSet: input is empty");
			return rsMap;
		}

		List<String> propList = DBProcess.getPropertyNameSet(ent);
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
	private List<String> getEntityBySolr(String sentence, boolean hasEntity) {
		List<String> rsEntitySet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getEntityBySolr: input is empty");
			return rsEntitySet;
		}

		SolrUtil solr = new SolrUtil();
		Solr_Query obj = new Solr_Query();
		obj.setFindEntity(hasEntity);
		NLPResult tnNode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			String segWord = segPos.get(i).word;
			if (!NLPProcess.isStopWord(segWord)) {
				obj.addWord(segWord);
			}
		}

		return solr.Search(obj);
	}

	// get the candidiates by spliting the sentence accroding to the entity
	// input: [你知道，的老婆的身高吗]（“你知道姚明的老婆的身高吗？”）
	// output: [老婆，身高]
	// if question does not contain ent, return null.
	private List<String> getCandidateSetbyStopWord(List<String> strSet) {
		List<String> rsList = new ArrayList<>();
		if (strSet == null) {
			System.err.println("PMP.getCandidateSet: input is empty");
		}

		for (String str : strSet) {
			String tempStr = str;
			NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
			List<Term> segPos = tnNode.getWordPos();
			for (int i = 0; i < segPos.size(); i++) {
				String segWord = segPos.get(i).word;
				if (NLPProcess.isStopWord(segWord)) {
					if (!tempStr.startsWith(segWord)) {
						rsList.add(tempStr.substring(0, tempStr.indexOf(segWord)));
					}
					tempStr = tempStr.substring(tempStr.indexOf(segWord) + segWord.length());
				}
			}
			// System.out.println("str is " + str + ", and list is " +
			// rsList.toString());
		}
		System.out.println("PM.getCandidateSetbyEntityandStopWord is " + rsList.toString());
		return rsList;
	}

	// may remove to Tool, and use this in getCandidateSet
	private List<String> splitSentenceByWord(String sentence, String word) {
		List<String> list = new ArrayList<>();
		if (!sentence.contains(word)) {
			list.add(sentence);
		}
		while (sentence.lastIndexOf(word) != -1) {
			String s = sentence.substring(sentence.lastIndexOf(word) + word.length()).trim();
			if (!s.isEmpty()) {
				list.add(s); // add the last part
			}

			// remove the last part
			sentence = sentence.substring(0, sentence.lastIndexOf(word)).trim();
			if (!sentence.isEmpty() && sentence.lastIndexOf(word) == -1) {
				list.add(sentence);
			}
		}
		return list;
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
			String s = str.substring(str.lastIndexOf(ent) + ent.length()).trim();
			if (!s.isEmpty()) {
				// System.out.println("PMP.GetCandidateSet, s ="+s);
				listPart.add(s); // add the last part
			}

			// remove the last part
			str = str.substring(0, str.lastIndexOf(ent)).trim();
			if (!str.isEmpty() && str.lastIndexOf(ent) == -1) {
				listPart.add(str);
				// System.out.println("PMP.GetCandidateSet: str=" + str);
			}
		}
		return listPart;
	}

	// remove the stopword in a string.
	// input: "身高是多少"
	// output: "身高"
	private String removeStopWord(String str) {
		String rs = "";
		// Segmentation Process
		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		System.out.println("original string is " + str);
		List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			String s = segPos.get(i).word;
			// System.out.print(s + ", ");
			if (!NLPProcess.isStopWord(s))
				rs += s;
		}
		// System.out.println("");

		return rs;
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

		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
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

	// remove stopword and generate candidates by synonym
	private List<String> auxilaryProcess(String str) {
		List<String> rsSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(str)) {
			System.err.println("PMP.auxilaryProcess: input is empty");
			return rsSet;
		}

		// 1. remove the stop words like "多少" in a sentence
		str = this.removeStopWord(str);
		System.out.println("PMP.auxilaryProcess: after removing Stop Words: " + str);

		// 2. generate candidates according to the synonyms
		Map<String, String> refPropMap = new HashMap<>();
		rsSet = this.replaceSynonymProcess(str, refPropMap);
		System.out.println("PMP.auxilaryProcess: after synonym process: " + rsSet + " & size is " + rsSet.size());

		return rsSet;
	}

	// return the property with the highest score; return null if the threshold
	// is hold the version without segPos
	// input: （姚明）妻
	// output: 叶莉
	private PatternMatchingResultBean getCandidatePropName(String candidate, Map<String, String> propMap,
			int originalScore) {
		// threshold to pass: if str contain a property in DB, pass
		boolean isPass = false;
		HashMap<String, Integer> rsMap = new HashMap<String, Integer>();
		PatternMatchingResultBean beanPM = new PatternMatchingResultBean();
		if (Tool.isStrEmptyOrNull(candidate) || propMap == null) {
			System.err.println("PMP.getCandidatePropName: input is empty");
			return beanPM;
		}

		System.out.println("query string is: " + candidate);

		for (String strProperty : propMap.keySet()) {
			// System.out.println("current prop is: " + s);

			if (!isPass && candidate.lastIndexOf(strProperty) != -1) {
				isPass = true;
			}

			// pattern matching algorithm suggested by Phantom
			// compute the score by scanning the sentence from left to right
			// compare each char, if match, socre++, else score--
			String tmpProp = strProperty;
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
			tmpProp = strProperty;
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

		}

		int finalScore = Integer.MIN_VALUE;
		for (String s : propMap.keySet()) {
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

	// template process, change the exception cases
	// input: entity and sentence, "姚明", "姚明多高"
	// output: the sentence changed by template, "姚明身高多少"
	private String templateProcess(String entity, String sentence) {
		if (sentence.lastIndexOf(entity) == -1 || sentence.equals(entity)) {
			return sentence;
		}

		String[] strArr = sentence.split(entity);
		if (strArr.length == 0) {
			return "";
		}

		String rs = strArr[0];
		for (int i = 1; i < strArr.length; i++) {
			rs += "## " + entity + "<type>entity</type> ";
			rs += strArr[i];
		}

		// if entity appear in the last
		if (sentence.lastIndexOf(entity) == sentence.length() - entity.length()) {
			rs += "## " + entity + "<type>entity</type> ";
		}

		// System.out.println("rs=" + rs);
		rs = sentenceTemplate.process(rs);
		if (rs.isEmpty()) {
			rs = sentence;
		}

		// System.out.println("input=" + sentence + ", output=" + rs);
		return rs;
	}

	// template process, change the exception cases for the cases with two
	// entities for 4/15 milestone
	// input: entity and sentence, "姚明和叶莉的宝宝是谁"
	// output: the sentence changed by template, "姚明和叶莉的孩子是谁"
	private String templateProcess(String entityA, String entityB, String sentence) {
		String rs = "";
		// System.out.println("input=" + sentence + ", output=" + rs);
		return rs;
	}

	public static void main(String[] args) {
		PatternMatchingProcess mp = new PatternMatchingProcess();
		String str = "姚明的妻子的身高？";
		mp.getAnswer(str);

		// AnswerBean answerBean = new AnswerBean();
		// System.out.println("RS=" + mp.ReasoningProcess(str, "姚明",
		// answerBean));

		// List<PatternMatchingResultBean> listPMBean = new
		// ArrayList<PatternMatchingResultBean>();
		// PatternMatchingResultBean bean = new PatternMatchingResultBean();
		// bean.setAnswer("身高");
		// listPMBean.add(bean);
		// PatternMatchingResultBean beanB = new PatternMatchingResultBean();
		// beanB.setAnswer("老婆");
		// listPMBean.add(beanB);
		// System.out.println("result is " + mp.mutlipleReasoningProcess("姚明",
		// listPMBean));

		List<String> lstr = new ArrayList<>();
		lstr.add("你知不知道");
		lstr.add(str);

		// mp.getCandidateSetbyStopWord(mp.getCandidateSet(str, "姚明"));
		// mp.templateProcess("姚明", str);

		// System.out.println("senType="+mp.templateProcess("姚明", str));
		// System.out.println("senType="+mp.sentenceClassifier.getSentenceType(mp.templateProcess("姚明",
		// str)));

		// String ent = "姚明";
		// Map<String, String> mapP = new HashMap<>();
		// mapP.put("最高分", "最高分");
		// mapP.put("生涯最高分", "生涯最高分");
		// mapP.put("生涯初场", "生涯初场");

		// mp.getCandidatePropName(str, mapP);

		// mp.getPropertyNameSet(ent);
		// mp.replaceSynonymProcess(str);

		// System.out.println("test aux:"+mp.getAnswer("姚明的妻子是谁？"));

		// System.out.println("syn is " + NLPProcess.getSynonymSet("脊"));
		// System.out.println("syn is " + NLPProcess.getSynonymSet("门房"));
		// System.out.println("syn is " + NLPProcess.isStopWord("门房"));
		// System.out.println("syn is " + NLPProcess.isStopWord("是"));
		// System.out.println("syn is " + NLPProcess.isStopWord("嘛"));

		/*
		 * System.out.println(mp.getAnswer(question));
		 * 
		 * System.out.println(ent.charAt(0) + "  " + ent.charAt(ent.length() -
		 * 1) + " index of " + ent.lastIndexOf("明") + " length=" +
		 * ent.length());
		 * 
		 * System.out.println("case is " + question);
		 * 
		 * // test for getPartsWithoutEntity System.out.println(
		 * "the position is " + question.lastIndexOf(ent) + " and lenth is " +
		 * question.length() + " ent lenth is " + ent.length()); int pos =
		 * question.lastIndexOf(ent); System.out.println("the pre is " +
		 * question.substring(0, pos) + ". And the post is " +
		 * question.substring(pos + ent.length()));
		 * 
		 * for (String s : mp.getPartsWithoutEntity(question, ent)) {
		 * System.out.println(s); }
		 * 
		 * question = mp.getPartsWithoutEntity(question, ent).get(0);
		 * System.out.println("after trancate: " + question);
		 * 
		 * // test for auxilary process question = mp.auxilaryProcess(question);
		 * 
		 * // test for Match Score List<String> listProp =
		 * DBProcess.getPropertyNameSet(ent); System.out.println("prop name is "
		 * + listProp); String propName = mp.getCandidatePropName(question,
		 * listProp);
		 */
	}

}
