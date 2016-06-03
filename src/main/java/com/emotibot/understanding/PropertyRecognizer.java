package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.common.Common;
import com.emotibot.template.TemplateEntry;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class PropertyRecognizer {

	private NERBean nerBean = new NERBean();

	public PropertyRecognizer(NERBean bean) {
		nerBean = bean;
	}

	// Multi-level Reasoning Understanding
	protected AnswerBean ReasoningProcess(String sentence, String label, String entity, AnswerBean answerBean) {
		System.out.println(
				"PMP.ReasoningProcess INIT: sentence=" + sentence + ", entity =" + entity + ", bean is " + answerBean);
		// Debug.printDebug(uniqueID, 3, "knowledge", "PMP.ReasoningProcess:
		// sentence=" + templateSentence + ", entity ="
		// + entity + ", bean is " + answerBean.toString());
		// to be checked later
		String oldSentence = sentence;
		sentence = TemplateEntry.templateProcess(label, entity, sentence, nerBean.getUniqueID());

		// print debug log
		if (nerBean.isDebug()) {
			String debugInfo = answerBean.getComments() + "\n template process: ilabel:" + label + " from:"
					+ oldSentence + " to:" + sentence;
			System.out.println(debugInfo);
			Debug.printDebug(nerBean.getUniqueID(), 3, "KG", debugInfo);
			answerBean.setComments(debugInfo);
		}

		if (!sentence.contains(entity)) {
			System.err.println("Sentence does not contain entity");
			Debug.printDebug(nerBean.getUniqueID(), 2, "knowledge", "Sentence does not contain entity");
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
		if (listPMBean.isEmpty() && QuestionClassifier.isKindofQuestion(nerBean.getSentence(),
				QuestionClassifier.introductionQuestionType, "")) {
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
				String newLabel = DBProcess.getEntityLabel(newDBEntity);
				String newSentence = sentenceNoEntity;
				if (!Tool.isStrEmptyOrNull(oldWord)) {
					newSentence = sentenceNoEntity.replace(oldWord, newDBEntity);
					newSentence = removeStopWordInSentence(newSentence);
				}
				System.out.println("-----> case 1 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				System.out.println("prop:" + prop + ", new sentence:" + newSentence);
				return ReasoningProcess(newSentence, newLabel, newDBEntity, answerBean);
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

			listPMBean = removeDuplicatedAnswerBean(listPMBean);
			
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
				String newLabel = DBProcess.getEntityLabel(newDBEntity);
				System.out.println("-----> case 2 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				String newSentence = sentenceNoEntity.replace(answerBean.getOriginalWord(), newDBEntity);
				newSentence = removeStopWordInSentence(newSentence);
				return ReasoningProcess(newSentence, newLabel, newDBEntity, answerBean);
			} else {
				answerBean.setAnswer(answer.substring(0, answer.length() - 1));
				answerBean.setScore(score);
				// answerBean.setValid(true);
				System.out.println("\t EndOfRP  @@ return case 2, answer = " + answerBean);
				return answerBean.returnAnswer(answerBean);
			}
		}
	}
	
	//去掉PatternMatchingResultBean list 里面针对多个相同的property 重复回答的 PatternMatchingResultBean
	private List<PatternMatchingResultBean> removeDuplicatedAnswerBean(List<PatternMatchingResultBean> patternMatchingResultBeans){
		List<PatternMatchingResultBean> listBeans = patternMatchingResultBeans;
		System.out.println(listBeans.size());
		Set<String> answerSet = new HashSet<String>();
		Iterator<PatternMatchingResultBean> iterator = listBeans.iterator();
		
		while (iterator.hasNext()) {
			PatternMatchingResultBean tempBean  = iterator.next();
			String tempProperty = tempBean.getAnswer();
			if(answerSet.contains(tempProperty)){
				iterator.remove();
			}else {
				answerSet.add(tempProperty);
			}
			
		}
		System.out.println(listBeans.size());
		return listBeans;
	}

	// Get the answer by the pattern matching method
	// for the case of single entity in multiple level reasoning case
	// input: the question sentence from users,"姚明的老婆的身高是多少"
	// output: the valid property contained in the sentence
	protected List<PatternMatchingResultBean> matchPropertyFromSentence(List<String> candidateSet,
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
			synList = replaceSynonymProcess(candidate, refPropMap);
			System.out.println("\t after synonym process: " + synList + " refPropMap=" + refPropMap);

			for (String syn : synList) {
				System.out.println("q=" + syn + " and s=" + candidate);
				int orignalScore = (syn.toLowerCase().equals(candidate.toLowerCase())) ? 100 : 80;
				PatternMatchingResultBean pmRB = new PatternMatchingResultBean();
				pmRB = recognizingProp(syn, propMap.keySet(), orignalScore);
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

	// generate all the possibility candidates according to synonyms
	// input: 这个标志多少
	// output: [这个记号数量, 这个标志数量, 这个记号多少, 这个标志多少]
	private List<String> replaceSynonymProcess(String str, Map<String, String> refMap) {
		 System.out.println("input of replaceSynonymProcess is " + str);
		List<String> rsSet = new ArrayList<>();
		if (str.isEmpty()) {
			System.out.println("output of replaceSynonymProcess is " + rsSet);
			return rsSet;
		}

		// NLPResult tnNode = NLPSevice.ProcessSentence(str,
		// NLPFlag.SegPos.getValue());
		// List<Term> segPos = tnNode.getWordPos();
		List<Term> segPos = NLPUtil.getSegWord(str);
		List<String> wordList = new ArrayList<>();
		
//		wordList.add(str);	// for fixing bug "泰山多高"
		for(int i = 0; i < segPos.size(); i++){
			String iWord = segPos.get(i).word;
			wordList.add(iWord);
		}
		
		rsSet.add("");
		
		
//		for (int i = 0; i < segPos.size(); i++) {
//			String iWord = segPos.get(i).word;
			
		for (int i = 0; i < wordList.size(); i++) {
			String iWord = wordList.get(i);
			// System.out.println("current word is " + iWord);
			Set<String> iSynSet = NLPUtil.getSynonymWordSet(iWord);
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

		 System.out.println("output of replaceSynonym: " + rsSet.toString());
		return rsSet;
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

	// get the candidiates by spliting the sentence accroding to the entity
	// input: [你知道，的老婆的身高吗]（“你知道姚明的老婆的身高吗？”）
	// output: [老婆，身高]
	// if question does not contain ent, return null.
	protected List<String> getCandidateSetbyStopWord(List<String> strSet) {
		List<String> rsList = new ArrayList<>();
		if (strSet == null) {
			System.err.println("PMP.getCandidateSetbyStopWord: input is empty");
			Debug.printDebug(nerBean.getUniqueID(), 2, "knowledge", "PMP.getCandidateSetbyStopWord: input is empty");
		}

		// System.err.println("PMP.getCandidateSetbyStopWord: input="+strSet);
		for (String str : strSet) {
			if (CharUtil.trim(str).isEmpty()) {
				continue;
			}
			// remove below for fixing case : "7号房的礼物是啥类型的电影"
			// rsList.add(str);

			String littleCandidate = "";
			List<Term> segPos = NLPUtil.getSegWord(str);
			System.out.println("PMP.getCandidateSetbyStopWord: segPos=" + segPos);

			for (int i = 0; i < segPos.size(); i++) {
				String segWord = segPos.get(i).word;
				if (!NLPUtil.isStopWord(segWord)) {
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

	// get the sentence by removing the stopword
	// input: “河南是哪儿？”
	// output: 河南
	protected String removeStopWordInSentence(String sentence) {
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.out.println("removeStopWordInSentence: sentence is empty");
			return "";
		}

		sentence = CharUtil.trimAndlower(sentence);
		List<Term> segPos = NLPUtil.getSegWord(sentence);
		System.out.println("removeStopWordInSentence: segPos=" + segPos);
		String strRS = "";
		for (int i = 0; i < segPos.size(); i++) {
			String segWord = segPos.get(i).word;
			if (!NLPUtil.isStopWord(segWord)) {
				strRS += segWord;
			}
		}

		System.out.println("\t removeStopWordInSentence is " + strRS);
		return strRS;
	}

	// get the candidiates by spliting the sentence accroding to the entity and
	// return the words after removing stop word
	// input: “斗罗大陆属于哪种小说”
	// output: [属于，哪种，小说]
	// if question does not contain ent, return null.
	protected List<String> getCandidateSetbyNLP(List<String> strList) {
		List<String> rsList = new ArrayList<>();
		if (strList == null) {
			System.err.println("PMP.getCandidateSetbyNLP: input is empty");
			Debug.printDebug(nerBean.getUniqueID(), 2, "knowledge", "null in getCandidateSetbyNLP");
			return rsList;
		}

		for (String str : strList) {
			List<Term> segPos = NLPUtil.getSegWord(str);
			for (int i = 0; i < segPos.size(); i++) {
				String segWord = CharUtil.trim(segPos.get(i).word);
				if (!segWord.isEmpty()) {
					rsList.add(segWord);
				}
			}
		}
		System.out.println("\t getCandidateSetbyNLP=" + rsList);
		Debug.printDebug(nerBean.getUniqueID(), 4, "knowledge", "\t getCandidateSetbyNLP=" + rsList);
		return rsList;
	}

	protected boolean hasPropertyInSentence(String sentence, String label, String entity) {
		System.out.println("hasPropertyInSentence: sentence=" + sentence + ", label=" + label + ", entity=" + entity);
		List<String> candidateSet = getCandidateSet(sentence, entity);
		List<String> candidateSetbyStopWord = getCandidateSetbyStopWord(candidateSet);
		Map<String, String> propMap = getPropertyNameSet(label, entity);

		if (matchPropertyFromSentence(candidateSetbyStopWord, propMap).isEmpty())
			return false;
		else
			return true;
	}

	// get the candidiates by spliting the sentence accroding to the entity
	// input: “你知道姚明的身高吗？”
	// output: [“你知道”，“的身高吗”]
	// if question does not contain ent, return null.
	protected List<String> getCandidateSet(String str, String ent) {
		List<String> listPart = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(str) || Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getCandidateSet: input is empty: str = " + str + ", ent = " + ent);
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

	// get the relationship set in DB with synonym process
	// return Map<synRelation, relationship>
	// input: 姚明
	// output: [<位置,位置>, <妻,老婆>, ...]
	protected Map<String, String> getRelationshipSet(String label, String ent) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getRelationshipSet: input is empty");
			return rsMap;
		}

		List<String> rList = DBProcess.getRelationshipSet(label, ent);
		for (String iRelation : rList) {
			rsMap.put(iRelation, iRelation);
			Set<String> setSyn = NLPUtil.getSynonymWordSet(iRelation);
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
				Set<String> setSyn = NLPUtil.getSynonymWordSet(iProp);
				for (String iSyn : setSyn) {
					rsMap.put(iSyn, iProp);
				}
			}
		}
		// System.out.println("all the prop is: " + rsMap);
		return rsMap;
	}

}
