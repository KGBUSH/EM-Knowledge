package com.emotibot.understanding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.log.LogService;
import com.emotibot.template.TemplateEntry;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class PropertyRecognizer {

	private NERBean nerBean = new NERBean();

	public PropertyRecognizer(NERBean bean) {
		nerBean = bean;
	}

	// from intention model. judge whether a sentence is a 'knowledge' sentence
	// chanwen added in 20160811
	public boolean isKnowledgeSentence(String sentence) {
		String fileName = Common.UserDir + "/config/KG.property";
		String intentServerIP = "";
		String intentServerPort = "";
		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String line = "";
				while ((line = dis.readLine()) != null) {
					if (line.startsWith("intent.server.ip")) {
						String[] words = CharUtil.trim(line).split("=");
						if(words.length >= 2)
							intentServerIP = CharUtil.trim(words[1]);	
					}
					if (line.startsWith("intent.server.port")) {
						String[] words = CharUtil.trim(line).split("=");
						if(words.length >= 2)
							intentServerPort = CharUtil.trim(words[1]);	
					}
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String urlStr = "http://"+ intentServerIP+":"+intentServerPort+"/?q="+ sentence;
		URL url = null;
		int state = -1;
		// HttpURLConnection httpConn = null;
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
		HttpURLConnection uc = null;
		try {
			url = new URL(urlStr);
			uc = (HttpURLConnection) url.openConnection();
			uc.setConnectTimeout(1000);
			uc.setReadTimeout(1000);
			state = uc.getResponseCode();
			if (state == 200) {
				in = new BufferedReader(new InputStreamReader(uc.getInputStream(),
						"UTF-8"));
				String str = null;
				while ((str = in.readLine()) != null) {
					sb.append(str);
				}
				in.close();
			} else {
				System.err.println("Connection error in method isKnowledgeSentence");
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			uc.disconnect();
		}
		String result = sb.toString();
		if (result.contains("Knowledge"))
			return true;
		else
			return false;
	}

	// Multi-level Reasoning Understanding
	protected AnswerBean ReasoningProcess(String sentence, String label, String entity, AnswerBean answerBean,
			String entityKey, boolean isTemplate) {
		System.out.println("PMP.ReasoningProcess INIT: sentence=" + sentence + ", label=" + label + ", entity ="
				+ entity + ", bean is " + answerBean);
		// Debug.printDebug(uniqueID, 3, "knowledge", "PMP.ReasoningProcess:
		// sentence=" + templateSentence + ", entity ="
		// + entity + ", bean is " + answerBean.toString());
		// to be checked later
		String oldSentence = sentence;
		// sentence = TemplateEntry.templateProcess(label, entity, sentence,
		// nerBean.getUniqueID());

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

		Map<String, String> relationMap = this.getRelationshipSet(label, entity, entityKey);
		System.out.println("\t relationMap = " + relationMap);

		// split the sentence by the entities to get candidates
		// get candidates by splitting the sentence by entities and stopwords.
		List<String> candidateSet = this.getCandidateSet(sentence, entity);
		List<String> candidateSetbyStopWord = this.getCandidateSetbyStopWord(candidateSet);
		System.out.println("PMP.ReasoningProcess: candidateSet = " + candidateSet);
		System.out.println("PMP.ReasoningProcess: candidateSetbyStopWord = " + candidateSetbyStopWord);

		// get all the property of the entity
		Map<String, String> propMap = this.getPropertyNameSet(label, entity, entityKey);
		System.out.println("PMP.ReasoningProcess: propMap = " + propMap);

		// get the matched properties by pattern matching method
		// first get the property by NOT segPos with NO stopword;
		// if match null, then by NOT segPos with stopword
		// if match null, then by segPos with stopword
		List<PatternMatchingResultBean> listPMBean = this.matchPropertyFromSentence(candidateSetbyStopWord, propMap);

		// add for introduction questions
		if (listPMBean.isEmpty()
				&& QuestionClassifier.isKindofQuestion(sentence, QuestionClassifier.introductionQuestionType, "")) {
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
		listPMBean = removeDuplicatedAnswerBean(listPMBean); // remove duplicate
		System.out.println("\t listPMBean=" + listPMBean);

		if (listPMBean.isEmpty()) {
			System.out.println("\t EndOfRP @@ return case 0, answer=" + answerBean);

			String templateSentence = TemplateEntry.templateProcess(label, entity, sentence, nerBean.getUniqueID());
			if (!isTemplate && !sentence.equals(templateSentence)) {
				System.out.println("teamplate case: return case 0");
				return ReasoningProcess(templateSentence, label, entity, answerBean, entityKey, true);
			}

			// does not match property, score decreases
			// remove for the case: “姚明的初中在哪里”
			// answerBean.setScore(answerBean.getScore() / 2);
			return answerBean.returnAnswer(answerBean);
		} else if (listPMBean.size() == 1) {
			String prop = listPMBean.get(0).getAnswer();
			String originalPropName = listPMBean.get(0).getOrignalWord();
			String answer = "";
			System.out.println("\t\t\t#### before Implication: prop = " + prop);
			if (ImplicationProcess.isImplicationWord(prop)) {
				System.out.print("\t\t\t#### Implication ");
				answer = ImplicationProcess.getImplicationAnswer(sentence, entity, prop, entityKey);
				// if (Tool.isStrEmptyOrNull(answer))
				// listPMBean.get(0).setScore(0);
				System.out.println("answer = " + answer);
			}

			if (answer.isEmpty()) {
				answer = DBProcess.getPropertyValue(label, entity, prop, entityKey);
			}
			
			//chanwen added in 20160818 for case "我想知道姚明的身高" is not knowledge type from intent
			// for example:井柏然的女友，我想知道刘德华的老婆
			String suffixSen = sentenceNoEntity;
			suffixSen = suffixSen.replaceAll("我想知道", "");
			suffixSen = suffixSen.replaceAll("我想了解", "");
			suffixSen = suffixSen.replaceAll("想知道", "");
			suffixSen = suffixSen.replaceAll("想了解", "");
			suffixSen = suffixSen.replaceAll("的", "");
			Set<String> suffixSenSynonym = NLPUtil.getSynonymWordSet(suffixSen);
			suffixSenSynonym.add(suffixSen);
			boolean isEntitywithProp = false;
			for (String s : suffixSenSynonym) {
				if (relationMap.containsKey(s)||propMap.containsKey(s)) {
					isEntitywithProp = true;
					break;
				}
			}
			
			//solve 名字抢答 这类case
			if(answer.trim().equals(entity)){
				System.out.println("remove the prop: "+ prop +" and answer: "+ answer);
				return answerBean;
			}
			
			answerBean.setAnswer(answer);
			answerBean.setProperty(prop);
			// answerBean.setValid(true);
			answerBean.setScore(listPMBean.get(0).getScore());
			String oldWord = listPMBean.get(0).getOrignalWord();
			answerBean.setOriginalWord(oldWord);
			System.out.println("\t answer = " + answerBean);

			// to test whether there is a corresponding property
			boolean furtherSeach = false;
			String relationProp = "";
			if (NLPUtil.isInSynonymDict(prop)) {
				for (String s : NLPUtil.getSynonymWordSet(prop)) {
					if (relationMap.containsKey(s)) {
						furtherSeach = true;
						relationProp = s;
						break;
					}
				}
			} else {
				furtherSeach = relationMap.containsKey(prop);
			}
			System.out.println("In Case 1 furtherSeach = " + furtherSeach);

			if (!isTemplate && furtherSeach && !relationProp.isEmpty()) {
				// use the new answer as new entity, get the entity as a whole
				Map<String, Object> tmpMap = DBProcess.getEntityByRelationship(label, entity, relationMap.get(relationProp),
						entityKey);
				System.out.println("tmpMap=" + tmpMap);
				String newDBEntity = (String) tmpMap.get(Common.KGNODE_NAMEATRR);
				String newLabel = NLPUtil.getLabelByEntity(newDBEntity);
				String newEntityKey = (String) tmpMap.get("key");
				String newSentence = sentenceNoEntity;
				System.out.println("nextEntity=" + newDBEntity + "; oldWord=" + oldWord + ", sentenceNoEntity="
						+ sentenceNoEntity);
				if (!Tool.isStrEmptyOrNull(oldWord)) {
					newSentence = sentenceNoEntity.replaceFirst(oldWord, newDBEntity);
					// newSentence = removeStopWordInSentence(newSentence);
				}

				System.out.println("-----> case 1 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				System.out.println("prop:" + prop + "relationProp:" + relationProp + ", new sentence:" + newSentence + ", newDBEntity=" + newDBEntity
						+ ", newLabel=" + newLabel + ", newEntityKey=" + newEntityKey);

				AnswerBean oldBean = (AnswerBean) answerBean.clone();
				AnswerBean tmpAnswerBean = ReasoningProcess(newSentence, newLabel, newDBEntity, answerBean,
						newEntityKey, false);
				// to prevent over mapping the template
				// case: 姚明的老婆多高
				if (!oldBean.equals(tmpAnswerBean)) {
					return tmpAnswerBean;
				}

				// return ReasoningProcess(newSentence, newLabel, newDBEntity,
				// answerBean, newEntityKey, false);
			}

			String templateSentence = TemplateEntry.templateProcess(label, entity, sentence, nerBean.getUniqueID());
			if (!isTemplate && !sentence.equals(templateSentence)) {
				System.out.println("teamplate case: return case 1");
				return ReasoningProcess(templateSentence, label, entity, answerBean, entityKey, true);
			}
			if (!isTemplate &&!isEntitywithProp&&!isKnowledgeSentence(sentence))
				answerBean.setScore(0);
				
			System.out.println("\t EndOfRP  @@ return case 1, answer=" + answerBean);
			
			return answerBean.returnAnswer(answerBean);

		} else {
			// in the case of multiple props, find a way out
			String answer = "";
			double score = 100;

			boolean furtherSeach = false;
			String prop = "";

			System.out.println("multi prop case: listPMBean = " + listPMBean);
			for (PatternMatchingResultBean b : listPMBean) {
				String queryAnswer = DBProcess.getPropertyValue(label, entity, b.getAnswer(), entityKey);
				prop = b.getAnswer();

				// to test whether there is a corresponding property
				if (NLPUtil.isInSynonymDict(prop)) {
					for (String s : NLPUtil.getSynonymWordSet(prop)) {
						if (relationMap.containsKey(s)) {
							prop = s;
							furtherSeach = true;
							break;
						}
					}
				} else {
					furtherSeach = relationMap.containsKey(prop);
				}
				System.out.println("b = " + b + ", futher serach = " + furtherSeach);

				if (furtherSeach) {
					System.out.println("case 2 furtherSeach = " + furtherSeach + " b = " + b);
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
			
			if(!answer.isEmpty()){
				answerBean.setAnswer(answer.substring(0, answer.length() - 1));
				answerBean.setScore(score);
			}

			if (!isTemplate && furtherSeach == true) {
				//地理位置 在判断是否在ralationship 里的时候通过同义词转换后没有替换为 所属地区，导致在这里获取的时候没有找到 
				// ######### bug #########
				Map<String, Object> tmpMap = DBProcess.getEntityByRelationship(label, entity, relationMap.get(prop),
						entityKey);
				String newDBEntity = (String) tmpMap.get(Common.KGNODE_NAMEATRR);
				String newLabel = NLPUtil.getLabelByEntity(newDBEntity);
				String newEntityKey = (String) tmpMap.get("key");
				Pattern pattern = Pattern.compile(answerBean.getOriginalWord());
				Matcher sentenceNoEntityWithPattern = pattern.matcher(sentenceNoEntity);
				String newSentence = sentenceNoEntityWithPattern.replaceFirst(newDBEntity);
				// String newSentence =
				// sentenceNoEntity.replace(answerBean.getOriginalWord(),
				// newDBEntity);
				// newSentence = removeStopWordInSentence(newSentence);

				System.out.println("-----> case 2 recurrence into: nextEntity=" + newDBEntity + "; Bean=" + answerBean);
				System.out.println("prop:" + prop + ", new sentence:" + newSentence + ", newDBEntity=" + newDBEntity
						+ ", newLabel=" + newLabel + ", newEntityKey=" + newEntityKey);
				// return ReasoningProcess(newSentence, newLabel, newDBEntity,
				// answerBean, newEntityKey, false);

				AnswerBean oldBean = (AnswerBean) answerBean.clone();
				AnswerBean tmpAnswerBean = ReasoningProcess(newSentence, newLabel, newDBEntity, answerBean,
						newEntityKey, false);
//				return tmpAnswerBean;
				if (!oldBean.equals(tmpAnswerBean)) {
					return tmpAnswerBean;
				}

			}

			// answerBean.setValid(true);
			System.out.println("\t EndOfRP  @@ return case 2, answer = " + answerBean);

			String templateSentence = TemplateEntry.templateProcess(label, entity, sentence, nerBean.getUniqueID());
			if (!isTemplate && !sentence.equals(templateSentence)) {
				System.out.println("teamplate case: return case 2");
				return ReasoningProcess(templateSentence, label, entity, answerBean, entityKey, true);
			}
			if (!isTemplate &&!isKnowledgeSentence(sentence))
				answerBean.setScore(0);

			return answerBean.returnAnswer(answerBean);

		}

		// sentence = TemplateEntry.templateProcess(label, entity, sentence,
		// nerBean.getUniqueID());

	}

	// 去掉PatternMatchingResultBean list 里面针对多个相同的property 重复回答的
	// PatternMatchingResultBean
	private List<PatternMatchingResultBean> removeDuplicatedAnswerBean(
			List<PatternMatchingResultBean> patternMatchingResultBeans) {
		List<PatternMatchingResultBean> listBeans = patternMatchingResultBeans;
		System.out.println(listBeans.size());
		Set<String> answerSet = new HashSet<String>();
		Iterator<PatternMatchingResultBean> iterator = listBeans.iterator();

		while (iterator.hasNext()) {
			PatternMatchingResultBean tempBean = iterator.next();
			String tempProperty = tempBean.getAnswer();
			boolean isRemoved = false;
			for (String s : NLPUtil.getSynonymWordSet(tempProperty)) {
				if (answerSet.contains(s)) {
					iterator.remove();
					isRemoved = true;
					break;
				}
			}

			if (!isRemoved) {
				// System.out.println("tempProperty="+tempProperty);
				answerSet.add(tempProperty);
				for (String s : NLPUtil.getSynonymWordSet(tempProperty)) {
					// System.out.println("s="+s);
					answerSet.add(s);
				}
			}

		}
		
		//fix bad case 丁俊晖的技术有什么特点？
		Set<String> answerSet2 = new HashSet<String>();
		Iterator<PatternMatchingResultBean> iterator2 = listBeans.iterator();
		
		while (iterator2.hasNext()) {
			PatternMatchingResultBean tempBean  = iterator2.next();
			String tempProperty = tempBean.getAnswer();
			if(answerSet2.contains(tempProperty)){
				iterator2.remove();
			}else {
				answerSet2.add(tempProperty);
			}
			
		}
		System.out.println("removeDuplicatedAnswerBean, listBeans=" + listBeans);
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
				// String orignalWord =
				// Tool.isStrEmptyOrNull(refPropMap.get(localPropName)) ?
				// localPropName
				// : refPropMap.get(localPropName);
				String orignalWord = candidate;
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
		// System.out.println("input of replaceSynonymProcess is " + str);
		List<String> rsSet = new ArrayList<>();
		if (str.isEmpty()) {
			System.out.println("output of replaceSynonymProcess is " + rsSet);
			return rsSet;
		}

		List<Term> segPos = NLPUtil.getSegWord(str);
		rsSet.add("");

		for (int i = 0; i < segPos.size(); i++) {
			String iWord = segPos.get(i).word;
			Set<String> iSynSet = NLPUtil.getSynonymWordSet(iWord);
			// System.out.println(" NLP iSynSet: " + iSynSet);

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
					System.out.println("\t iSyn is " + iSyn);
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
				// System.out.println("current word is " + iWord);
				// System.out.println("after syn is: " + newRS);
			} else {
				System.err.println("PMP.replaceSynonym: No syn: " + iWord);
				for (int j = 0; j < rsSet.size(); j++) {
					rsSet.set(j, rsSet.get(j) + iWord);
				}
			}
		}

		// System.out.println("replaceSynonymProcess.segPos="+segPos);
		if (segPos.size() > 1) {
			// System.out.println("replaceSynonymProcess.rsSet="+rsSet);
			Set<String> iSynSet = NLPUtil.getSynonymWordSet(str);
			for (String s : iSynSet) {
				rsSet.add(s);
			}
		}

		// System.out.println("output of replaceSynonym: " + rsSet.toString());
		return rsSet;
	}

	// return the property with the highest score; return null if the threshold
	// is hold the version without segPos
	// input: （姚明）妻
	// output: 叶莉
	private PatternMatchingResultBean recognizingProp(String candidate, Set<String> propSet, int originalScore) {
		System.out.println("init of recognizingProp: candidate=" + candidate + ", originalScore=" + originalScore);
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
			// System.out.print("strProperty="+strProperty);
		}
		// System.out.println("isPass="+isPass);

		int finalScore = Integer.MIN_VALUE;

		// 对得分相同的property进行处理
		List<String> sameValuePropetyList = new ArrayList<String>();
		for (String s : propSet) {
			if (rsMap.get(s) < 0) {
				continue;
			}
			if (rsMap.get(s) > finalScore) {
				sameValuePropetyList.clear();
				sameValuePropetyList.add(s);
				finalScore = rsMap.get(s);
				continue;
			}
			if (rsMap.get(s) == finalScore) {
				sameValuePropetyList.add(s);
			}

		}

		if (sameValuePropetyList.size() > 0) {
			String result = getBestAnswerFromCandidate(sameValuePropetyList, candidate);
			beanPM.setAnswer(result);
			beanPM.setScore(rsMap.get(result));
		} else {
			beanPM.setAnswer("");
			beanPM.setScore(-5);
		}

		// int score = Integer.MIN_VALUE;
		// Set<String> entrySet = rsFinalMap.keySet();
		// for(String s:entrySet){
		// if (rsFinalMap.get(s) > score){
		// score = rsFinalMap.get(s);
		// beanPM.setAnswer(s);
		// beanPM.setScore(rsMap.get(s));
		// }
		// }

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

	// 对topN 个答案进行帅选。
	private String getBestAnswerFromCandidate(List<String> topN, String candidate) {
		String finalResult = "";
		HashMap<String, Integer> rsFinalMap = new HashMap<String, Integer>();
		List<String> listCandidate = new ArrayList<>();
		// 迭代topN
		Iterator<String> iterator = topN.iterator();
		while (iterator.hasNext()) {
			// tempPro = topN 里的元素
			String tempCandidate = candidate;
			String tempPro = (String) iterator.next();
			int rightToLeft = 0;
			if (!tempPro.isEmpty()) {
				if (tempPro.endsWith(tempCandidate) || tempCandidate.endsWith(tempPro)) {
					rsFinalMap.put(tempPro, 5);
					listCandidate.add(tempPro);
				} else {
					for (int i = tempPro.length() - 1; i >= 0; i--) {
						if (tempCandidate.isEmpty()) {
							break;
						}
						if (!tempCandidate.isEmpty()
								&& tempCandidate.lastIndexOf(tempPro.charAt(i)) == tempCandidate.length() - 1) {
							rightToLeft++;
							tempCandidate = tempCandidate.substring(0, tempCandidate.length() - 1);
						} else {
							rightToLeft--;
						}
					}
					// 将结果放入rsMap
					rsFinalMap.put(tempPro, rightToLeft);
					listCandidate.add(tempPro);
				}
			}
		}

		int score = Integer.MIN_VALUE;
		// Set<String> entrySet = rsFinalMap.keySet();
		for (String s : listCandidate) {
			if (rsFinalMap.get(s) > score) {
				score = rsFinalMap.get(s);
				finalResult = "";
				finalResult += s;
			}
		}

		return finalResult;
	}

	// test the similarity between target (strProperty) and ref (candidate)
	private boolean SinglePatternMatching(HashMap<String, Integer> rsMap, String strProperty, String candidate,
			boolean isPass) {
		// System.out.println(">>>SinglePatternMatching: rsMap = " + rsMap +
		// "\t" + "strProperty=" + strProperty
		// + ", candidate=" + candidate);

		// case of length == 1

		if (Tool.isStrEmptyOrNull(strProperty) || Tool.isStrEmptyOrNull(candidate)) {
			System.err.println(
					"wrong format in SinglePatternMatching: strProperty=" + strProperty + ", candidate" + candidate);
			LogService.printLog("", "SinglePatternMatching",
					"wrong format in SinglePatternMatching: strProperty=" + strProperty + ", candidate" + candidate);
			return isPass;
		}
		
		/*if (strProperty.equals(candidate)) {
			rsMap.put(strProperty, 5);
			isPass = true;
		} else {
			rsMap.put(strProperty, Integer.MIN_VALUE);
		}*/

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
		int countTimesLeft = 0;
		int countTimesRight = 0;
		for (int i = 0; i < candidate.length(); i++) {
			if (tmpProp.indexOf(candidate.charAt(i)) == 0) {
				left2right++;
				countTimesLeft++;
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
				countTimesRight++;
				tmpProp = tmpProp.substring(0, tmpProp.length() - 1);
			} else {
				right2left--;
			}
		}

		// System.out.println("strProperty=" + strProperty + ", candidate=" +
		// candidate + "left is " + left2right + "right is " + right2left
		// + " isPass is " + isPass);

		// if (left2right != right2left)
		// System.err.println(
		// "sentence is " + str + ", prop is " + s + "; left=" + left2right
		// + ", right=" + right2left);

		if (left2right > right2left) {
			// fix bad case 次总冠军 －－ 总冠军数目 将匹配上的字符个数 >=3与得分 >= 2 的情况提升到5分
			if (countTimesLeft >= 3 && left2right >= 2) {
				rsMap.put(strProperty, 5);
			} else {
				rsMap.put(strProperty, left2right);
			}
		} else {
			if (countTimesRight >= 3 && right2left >= 2) {
				rsMap.put(strProperty, 5);
			} else {
				rsMap.put(strProperty, right2left);
			}
		}

		if (left2right == 0 && right2left == 0) {
			rsMap.put(strProperty, 5);
		}

		if (left2right >= 0 && right2left >= 0 && rsMap.get(strProperty) != 5) {
			LogService.printLog("", "SinglePatternMatching", "strProperty=" + strProperty + ", candidate=" + candidate
					+ ", left=" + left2right + ", right=" + right2left);
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
		Map<String, String> propMap = getPropertyNameSet(label, entity, "");

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

		if (!str.contains(ent)) {
			String entSyn = NLPUtil.getEntitySynonymReverse(ent);
			if (str.contains(entSyn)) {
				ent = entSyn;
			}
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
	protected Map<String, String> getRelationshipSet(String label, String ent, String entityKey) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getRelationshipSet: input is empty");
			return rsMap;
		}

		List<String> rList = DBProcess.getRelationshipSet(label, ent, entityKey);
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
	private Map<String, String> getPropertyNameSet(String label, String ent, String key) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getPropertyNameSet: input is empty");
			return rsMap;
		}

		List<String> propList = DBProcess.getPropertyNameSet(label, ent, key);
		// System.out.println("getPropertyNameSet.propList="+propList);
		if (propList != null && !propList.isEmpty()) {
			for (String iProp : propList) {
				rsMap.put(iProp, iProp);
				Set<String> setSyn = NLPUtil.getSynonymWordSet(iProp);
				// Set<String> setSyn = getSynonymSetOfProperty(iProp);
				for (String iSyn : setSyn) {
					rsMap.put(iSyn, iProp);
				}
			}
		}
		// System.out.println("all the prop is: " + rsMap);
		return rsMap;
	}

	// function: get the synonym word set of a property
	// method: get the synonym from syn table based on the segments of the
	// property
	// input: a property name, e.g.: 海拔高度
	// output: the synonym word set of this property, e.g.: 海拔身高, (since 身高 is
	// synonym of 海拔)
	private Set<String> getSynonymSetOfProperty(String prop) {
		// System.out.println("\n start getSynonymSetOfProperty: prop="+prop);
		Set<String> rtnSynSet = new HashSet<>();
		if (Tool.isStrEmptyOrNull(prop)) {
			return rtnSynSet;
		}

		// 1. get the synonym set of the whole property
		Set<String> setSyn = NLPUtil.getSynonymWordSet(prop);
		for (String s : setSyn) {
			rtnSynSet.add(s);
		}

		// 2. split the property into segments, get the syn set for each
		// segments, and then combine them together
		Map<String, String> refPropMap = new HashMap<>();
		List<String> synList = replaceSynonymProcess(prop, refPropMap);
		System.out.println("synList=" + synList);
		for (String s : synList) {
			rtnSynSet.add(s);
		}

		// System.out.println("end getSynonymSetOfProperty:
		// rtnSynSet="+rtnSynSet+"\n");
		return rtnSynSet;
	}

}
