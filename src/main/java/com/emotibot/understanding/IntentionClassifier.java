package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;

import com.emotibot.Debug.Debug;
import com.emotibot.WebService.AnswerBean;
import com.emotibot.answerRewrite.AnswerRewrite;
import com.emotibot.common.Common;
import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.template.TemplateEntry;
import com.emotibot.util.Tool;
import com.sun.tools.classfile.Annotation.element_value;

public class IntentionClassifier {

	private NERBean nerBean = new NERBean();

	public IntentionClassifier(){
		
	}
	
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
		
		
		List<String> labelListForRewritePart = new ArrayList<String>();
		
		//deal with the sentence returned by rewrite
		if(isSentenceByRewrite(sentence)){
			String stringRewritePart = sentence.substring(sentence.indexOf(":") + 1, sentence.indexOf("]")).trim();
			if(Tool.isStrEmptyOrNull(stringRewritePart)){
				answerBean.setValid(true);
				return answerBean.returnAnswer(answerBean);
			}else {
				//more than one label 
				if(stringRewritePart.contains("##")){
					//小说##电视剧
					String[] strList = stringRewritePart.split("##");
					labelListForRewritePart.add(NLPUtil.getLabelByDomainChineseName(strList[0]));
					labelListForRewritePart.add(NLPUtil.getLabelByDomainChineseName(strList[1]));
				}else {
					labelListForRewritePart.add(NLPUtil.getLabelByDomainChineseName(stringRewritePart));
				}
			}
			
			//rewrite = "\"["+answer+"],[" + template+"]\"";
			
			if(labelListForRewritePart.size() == 1){
				String result = DBProcess.getEntityIntroduction(entitySet.get(0),labelListForRewritePart.get(0));
				if(result.contains("。"))
					result = result.substring(0, result.indexOf("。"));
				String resultAdded = "";
				// add problem like 你想知道姚明的老婆是谁吗？
				if(NLPUtil.isContainsInDomainNeededToRewrite(labelListForRewritePart.get(0))){
					List<String> listquestions = getRelationOrPropertyByEntityAndConvertToSentence(entitySet.get(0),labelListForRewritePart.get(0));
					if(!listquestions.isEmpty()){
						// generate random number [0,listquestions.size()]
						int id = (int) Math.round(Math.random() * (listquestions.size() - 1));
						resultAdded = listquestions.get(id);
					}else {
						result = answerRewite.rewriteAnswer4Intro(result);
					}
				}else {
					result = answerRewite.rewriteAnswer4Intro(result);
				}
				if(!resultAdded.isEmpty() &&!resultAdded.equals("")){
					result = Tool.combineTwoResult(result, resultAdded);
					answerBean.setIntent(resultAdded);
					answerBean.setIntent(true);
				}
				answerBean.setAnswer(result);
				answerBean.setScore(100);
				return answerBean.returnAnswer(answerBean);
			}else if(labelListForRewritePart.size() == 2){
				String result1 = DBProcess.getEntityIntroduction(entitySet.get(0),labelListForRewritePart.get(0));
				String result2 = DBProcess.getEntityIntroduction(entitySet.get(0),labelListForRewritePart.get(1));
				if (result1.contains("。"))
					result1 = result1.substring(0, result1.indexOf("。"));
				if (result2.contains("。"))
					result2 = result2.substring(0, result2.indexOf("。"));
				String result = Tool.combineTwoResult(result1, result2);
				answerBean.setAnswer(result);
				answerBean.setScore(100);
				return answerBean.returnAnswer(answerBean);
			}else {
				System.err.println("label getted from rewrite function is error");
			}
			
		}

		System.out.println("##### sentence" + sentence + ", entitySet=" + entitySet + ", getOldSentence()="+nerBean.getOldSentence());
		if (entitySet.size() == 1 && entitySet.get(0).equals(sentence)) {
			// synonym case that is a high frequent word, do not answer this
			// kind of entity
			if (!sentence.equals(nerBean.getOldSentence()) && NLPUtil.isInHighFrequentDict(nerBean.getOldSentence())) {
				answerBean.setValid(true);	// set valid then the answer will be returned
				return answerBean.returnAnswer(answerBean);
			}
			
			if (NLPUtil.isInHighFrequentDict(sentence)) {
				answerBean.setValid(true);	// set valid then the answer will be returned
				return answerBean.returnAnswer(answerBean);
			}

			System.out.println("Single Entity Case: entity=" + entitySet.get(0));
			String tempEntity = entitySet.get(0);
			if(!NLPUtil.isDBEntity(tempEntity) && NLPUtil.isASynonymEntity(tempEntity)){
				tempEntity = NLPUtil.getEntitySynonymNormal(tempEntity).get(0);
				System.out.println("synonym case: syn is "+entitySet.get(0)+", and real entity is "+tempEntity);
			}

			System.out.println("INTENTION 1");
			String tempLabel = NLPUtil.getLabelByEntity(tempEntity).toLowerCase();

			// if (NLPUtil.isInRemoveableAllDict(tempEntity)) {
			if (NLPUtil.isInHighFrequentDict(tempEntity) || NLPUtil.isInDailyUsedWordDict(tempEntity)) {
				System.out.println(
						"high frequent word not in the whitelist domain case, and abord， the returned anwer is "
								+ answerBean.toString());
				answerBean.setValid(true);	// set valid then the answer will be returned
				return answerBean;
			}
			
			// if (tempLabel.equals("catchword")) {
			// if (!NLPUtil.isInDomainWhiteListDict(tempLabel)) {
			if (NLPUtil.isInDomainBalckListDict(tempLabel)) {
				System.out.println("catchword Case, and abord， the returned anwer is " + answerBean.toString());
				answerBean.setValid(true);	// set valid then the answer will be returned
				return answerBean;
			}

			/**
			 * 开始处理rewrite 的多义词情况。
			 */
			List<String> labelList = NLPUtil.getLabelListByEntity(tempEntity);
			List<String> finalLabelList1 = getFinalLabelListOfCase1(labelList);

			// judge whether labelListResult contains more than one label
			if (finalLabelList1.size() > 1) {
				return getAnswerOfCase1(finalLabelList1);
			}
						
//			String tempStrIntroduce = DBProcess.getPropertyValue(entitySet.get(0),
//					Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			String tempStrIntroduce = DBProcess.getEntityIntroduction(tempEntity,tempLabel);
			if (tempStrIntroduce.contains("。"))
				tempStrIntroduce = tempStrIntroduce.substring(0, tempStrIntroduce.indexOf("。"));
			
//			String answerAfterRewrite = answerRewite.rewriteAnswer4Intro(tempStrIntroduce);
			
			// add problem like 你想知道姚明的老婆是谁吗？
			String resultAdded = "";
			if(NLPUtil.isContainsInDomainNeededToRewrite(tempLabel)){
				List<String> listquestions = getRelationOrPropertyByEntityAndConvertToSentence(tempEntity,tempLabel);
				if(!listquestions.isEmpty()){
					// generate random number [0,listquestions.size()]
					int id = (int) Math.round(Math.random() * (listquestions.size() - 1));
					resultAdded = listquestions.get(id);
				}else {
					tempStrIntroduce = answerRewite.rewriteAnswer4Intro(tempStrIntroduce);
				}
			}else {
				tempStrIntroduce = answerRewite.rewriteAnswer4Intro(tempStrIntroduce); 
			}
			if(!resultAdded.isEmpty() &&!resultAdded.equals("")){
				tempStrIntroduce = Tool.combineTwoResult(tempStrIntroduce, resultAdded);
				answerBean.setIntent(resultAdded);
				answerBean.setIntent(true);
			}
			
			answerBean.setAnswer(tempStrIntroduce);
			
			if (NLPUtil.isInDomainWhiteListDict(tempLabel)) {
				answerBean.setScore(100);
			} else {
				// change the score after the decision of the topic for solo entity
				answerBean.setScore(0);
				answerBean.setValid(true);	// set valid then the answer will be returned
			}
			System.out.println("intentionProcess intro 1: the returned anwer is " + answerBean.toString());
			return answerBean.returnAnswer(answerBean);
		}

		System.out.println("INTENTION 2, after Single Entity");

		// move the process of introduction question to intention process
		if (entitySet.size() == 1) {
			String tempEntity = entitySet.get(0);
			if(!NLPUtil.isDBEntity(tempEntity) && NLPUtil.isASynonymEntity(tempEntity)){
				tempEntity = NLPUtil.getEntitySynonymNormal(tempEntity).get(0);
				sentence = sentence.toLowerCase().replace(entitySet.get(0), tempEntity);
				System.out.println("Intentinon 2: Synonym case tempEntity = "+tempEntity);
			} else {
				System.out.println("Intentinon 2: normal case tempEntity = "+tempEntity);
			}
			String tempLabel = NLPUtil.getLabelByEntity(tempEntity);
//			String tempSentence = TemplateEntry.templateProcess(tempLabel, tempEntity, sentence, uniqueID);

			// print debug log
			if (Common.KG_DebugStatus || nerBean.isDebug()) {
				String tmpLabel = "";
				if (!entitySet.isEmpty()) {
					tmpLabel = NLPUtil.getLabelByEntity(entitySet.get(0));
				}
				String debugInfo = "DEBUG: userSentence=" + sentence + "; entitySet=" + entitySet + "; label="
						+ tmpLabel;
				
				String tempSentence = TemplateEntry.templateProcess(tempLabel, tempEntity, sentence, uniqueID);
				debugInfo += "; template change to:" + tempSentence;
				answerBean.setComments(debugInfo);
				System.out.println(debugInfo);
				Debug.printDebug("123456", 1, "KG", debugInfo);
			}

			if (NLPUtil.isInRemoveableAllDict(tempEntity) || NLPUtil.isInDailyUsedWordDict(tempEntity)) {
				System.out.println("high frequent word in the blacklist domain case, and abord， the returned anwer is "
						+ answerBean.toString());
				return answerBean;
			}
			
			// when a sentence is not match by above introduction template then goto the introduciton template by domain
			// 你知道电视剧三国演义吗？ isIntroductionRequestByDomain() 在novel 里找不到会去tv 里
			List<String> listLabel = NLPUtil.getLabelListByEntity(tempEntity);
			if(!listLabel.isEmpty()){
				for(String label : listLabel){
					if(NLPUtil.isIntroductionDomainTable(label)){
						
						boolean isIntroductionByDomain = QuestionClassifier.isIntroductionRequestByDomain(label, NLPUtil.removePunctuateMark(NLPUtil.removeMoodWord(tempEntity, sentence)), tempEntity);
						if(isIntroductionByDomain){
							String strIntroduceByDomain = DBProcess.getEntityIntroduction(tempEntity,label);
							if(strIntroduceByDomain.contains("。"))
								strIntroduceByDomain = strIntroduceByDomain.substring(0, strIntroduceByDomain.indexOf("。"));
							
//							String answerAfterRewrite  = answerRewite.rewriteAnswer4Intro(strIntroduceByDomain);
							// add problem like 你想知道姚明的老婆是谁吗？
							
							String resultAdded = "";
							if(NLPUtil.isContainsInDomainNeededToRewrite(label)){
								List<String> listquestions = getRelationOrPropertyByEntityAndConvertToSentence(tempEntity,label);
								if(!listquestions.isEmpty()){
									// generate random number [0,listquestions.size()]
									int id = (int) Math.round(Math.random() * (listquestions.size() - 1));
									resultAdded = listquestions.get(id);
								}else {
									strIntroduceByDomain = answerRewite.rewriteAnswer4Intro(strIntroduceByDomain);
								}
							}else {
								strIntroduceByDomain = answerRewite.rewriteAnswer4Intro(strIntroduceByDomain);
							}
							if(!resultAdded.isEmpty() &&!resultAdded.equals("")){
								strIntroduceByDomain = Tool.combineTwoResult(strIntroduceByDomain, resultAdded);
								answerBean.setIntent(resultAdded);
								answerBean.setIntent(true);
							}
							
							answerBean.setScore(100);
							answerBean.setAnswer(strIntroduceByDomain);
							System.out.println("intentionProcess intro 3: the returned anwer is " + answerBean.toString());
							return answerBean.returnAnswer(answerBean);
						}
					}
				}
			}else {
				System.err.println("labellist of entity" +tempEntity+" is null");
				return answerBean;
			}
			
			boolean isIntro = QuestionClassifier.isIntroductionRequest(NLPUtil.removePunctuateMark(NLPUtil.removeMoodWord(tempEntity, sentence)),
					tempEntity);
			if (isIntro) {
				/**
				 * 开始处理rewrite 的多义词情况。
				 */
				List<String> labelList = NLPUtil.getLabelListByEntity(tempEntity);
				
				List<String> finalLabelList2 = getFinalLabelListOfCase1(labelList);
				
				// judge whether labelListResult contains more than one label
				if (finalLabelList2.size() > 1) {
					return getAnswerOfCase1(finalLabelList2);
				}
				
//				String strIntroduce = DBProcess.getPropertyValue(tempEntity, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
				String strIntroduce = DBProcess.getEntityIntroduction(tempEntity,tempLabel);
				if (strIntroduce.contains("。"))
					strIntroduce = strIntroduce.substring(0, strIntroduce.indexOf("。"));
//				String answerAfterRewrite  = answerRewite.rewriteAnswer4Intro(strIntroduce);
				// add problem like 你想知道姚明的老婆是谁吗？
				
				String resultAdded = "";
				if(NLPUtil.isContainsInDomainNeededToRewrite(tempLabel)){
					List<String> listquestions = getRelationOrPropertyByEntityAndConvertToSentence(tempEntity,tempLabel);
					if(!listquestions.isEmpty()){
						// generate random number [0,listquestions.size()]
						int id = (int) Math.round(Math.random() * (listquestions.size() - 1));
						resultAdded = listquestions.get(id);
					}else {
						strIntroduce = answerRewite.rewriteAnswer4Intro(strIntroduce);
					}
				}else {
					strIntroduce = answerRewite.rewriteAnswer4Intro(strIntroduce);
				}
				if(!resultAdded.isEmpty() &&!resultAdded.equals("")){
					strIntroduce = Tool.combineTwoResult(strIntroduce, resultAdded);
					answerBean.setIntent(resultAdded);
					answerBean.setIntent(true);
				}
				
				answerBean.setScore(100);
				answerBean.setAnswer(strIntroduce);
				System.out.println("intentionProcess intro 2: the returned anwer is " + answerBean.toString());
				return answerBean.returnAnswer(answerBean);
			}
		}
		System.out.println("INTENTION 3");

		return answerBean;
	}

	//judge whether a sentence is rewrited.
	private boolean isSentenceByRewrite(String str){
		String string = str.trim();
		if((string.contains("[Rewrite:") || string.contains("[rewrite:"))&&string.endsWith("]")){
			return true;
		}else {
			return false;
		}
	}
	
	//如果listLabel 里面有大于2个label，只返回前面两个，如果有一个，就返回一个label 
	public static List<String> getTheFrist2Labels(List<String> listLabel) {
		List<String> list = new ArrayList<String>();
		// 如果labelListResult 里面的label大于三个，只取前面两个。如果有一个，就取第一个。
		Iterator<String> iterator = listLabel.iterator();
		int count = 0;
		while(iterator.hasNext()){
			if(count > 1){
				break;
			}
			list.add(iterator.next());
			count++;
		}
		return list;
	}
	
	//get final answer by label list that has been dealed with.
	public static AnswerBean getAnswerOfCase1(List<String> list){
		AnswerBean answerBean = new AnswerBean();
		String labelAChineseName = NLPUtil.getDomainChineseNameByLabel(list.get(0));
		String labelBChineseName = NLPUtil.getDomainChineseNameByLabel(list.get(1));
		String sentenceOfAnswer1 = "你指的是"+ labelAChineseName + "还是"+ labelBChineseName + "呀？";
		answerBean.setAnswer(sentenceOfAnswer1);
		answerBean.setScore(100);
		return answerBean.returnAnswer(answerBean);
	}
	
	// remove the label that not contains in the label table provided by pm and 
	//deal with label list to size = 1 or 2  
	public static List<String> getFinalLabelListOfCase1(List<String> labelList){
		List<String> labelListResult = new ArrayList<String>();
		for (String string : labelList) {
			if (NLPUtil.isContainsInDomainNameMappingTable(string)
					&& !Tool.isStrEmptyOrNull(string)) {
				labelListResult.add(string);
			}
		}
		
		return getTheFrist2Labels(labelListResult);
		
	}
	
	//return a sentence combined by entity and Relation or Property list orderby pm. 
	//input 姚明  
	//output 你想知道姚明的好友吗？ 你想知道姚明的特长吗？
	public Map<String, String> getRelationOrPropertyByEntityAndConvertToSentence(String ent){
		String entity = "";
		if(NLPUtil.isDBEntity(ent)){
			entity = ent;
		}else if(NLPUtil.isEntity(ent)){
			entity = NLPUtil.getEntitySynonymNormal(ent).get(0);
		}else {
			System.err.println("entity:"+ ent +"is not found in neo4j");
			return new HashMap<String,String>();
		}
		List<String> listMiddle = new ArrayList<String>();
		Map<String,String> MapResult = new HashMap<String,String>();
		Set<String> setTemp = new HashSet<String>();
		String label = NLPUtil.getLabelByEntity(entity);
		List<String> tempListProperty = DBProcess.getEntityPropertyList(entity, label);
		for(String str : tempListProperty){
			if(!setTemp.contains(str))
				setTemp.add(str);
		}
		
		if (label.equals("figure")) {
			List<String> tempListRelatin = DBProcess.getEntityRelationList(
					entity, label);
			for (String str : tempListRelatin) {
				if (!setTemp.contains(str))
					setTemp.add(str);
			}
			for (String str1 : NLPUtil.getRelationOrPropertyByEntity(label,
					"relation")) {
				for (String str2 : NLPUtil.getSynonymWordSet(str1)) {
					if (setTemp.contains(str2)) {
						listMiddle.add(str2);
						break;
					}
				}
			}
		}

		//judge whether a label contains in the label list that pm provided.
		if(NLPUtil.isContainsInDomainNeededToRewrite(label)){
			for (String string : NLPUtil.getRelationOrPropertyByEntity(label,
					"property")) {
				for (String str : NLPUtil.getSynonymWordSet(string)) {
					if (setTemp.contains(str)) {
						listMiddle.add(str);
						break;
					}
				}
			}
		}
		
		if(!listMiddle.isEmpty()){
			// <entity + "的" + str,"你想知道" + entity + "的" + str + "吗？">
			for (String str : listMiddle) {
				MapResult.put(entity + "的" + str, "你想知道" + entity + "的" + str + "吗？");
			}
		}
		
		return MapResult;
	}
	
	// return a sentence combined by entity and Relation or Property list
	// orderby pm.
	// input 姚明
	// output 你想知道姚明的好友吗？ 你想知道姚明的特长吗？
	public static List<String> getRelationOrPropertyByEntityAndConvertToSentence(
			String entity, String label) {
		List<String> listMiddle = new ArrayList<String>();
		List<String> listResult = new ArrayList<String>();
		Set<String> setTemp = new HashSet<String>();
		List<String> tempListProperty = DBProcess.getEntityPropertyList(entity,
				label);
		for (String str : tempListProperty) {
			if (!setTemp.contains(str))
				setTemp.add(str);
		}

		if (label.equals("figure")) {
			List<String> tempListRelatin = DBProcess.getEntityRelationList(
					entity, label);
			for (String str : tempListRelatin) {
				if (!setTemp.contains(str))
					setTemp.add(str);
			}
			for (String str1 : NLPUtil.getRelationOrPropertyByEntity(label,
					"relation")) {
				for (String str2 : NLPUtil.getSynonymWordSet(str1)) {
					if (setTemp.contains(str2)) {
						listMiddle.add(str2);
						break;
					}
				}
			}
		}

		//judge whether a label contains in the label list that pm provided.
		if(NLPUtil.isContainsInDomainNeededToRewrite(label)){
			for (String string : NLPUtil.getRelationOrPropertyByEntity(label,
					"property")) {
				for (String str : NLPUtil.getSynonymWordSet(string)) {
					if (setTemp.contains(str)) {
						listMiddle.add(str);
						break;
					}
				}
			}
		}else {
			for(String str : tempListProperty){
				listMiddle.add(str);
			}
		}
		

		for (String str : listMiddle) {
			listResult.add("你想知道" + entity + "的" + str + "吗？");
		}
		return listResult;
	}
	
	public static void main(String[] args) {
		DictionaryBuilder.DictionaryBuilderInit();
//		List<String> list = getRelationOrPropertyByEntityAndConvertToSentence("叶璇");
//		System.out.println(list);
	}
}
