package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.hankcs.hanlp.seg.common.Term;

public class PatternMatchingProcess {

	private final String[] auxList = { "多少", "吗", "是", "有", "的" };

	/*
	 * Get the answer by the match score method input: the question sentence
	 * from users output: the answer without answer rewriting
	 */
	public String getAnswer(String question) {

		String rs = "";
		/*
		 * 1. get the entity by Solr
		 */
		String ent = getEntityBySolr(question);

		/*
		 * 2. split the sentence by the entity and get two sub parts: the
		 * previous and the latter one 2.1 remove the auxiliary words like "多少"
		 */
		// get the last one for 3/15, may improve latter
		Set<String> candidateSet = this.getPartsWithoutEntity(question, ent);

		// 3. compute the score and get the candidate
		Map<String, String> propMap = this.getPropertyNameSet(ent);
		List<PatternMatchingResultBean> rsBean = new ArrayList();
		String propName = "";

		for (String s : candidateSet) {
			System.out.println("Candidate is " + s);
			List<String> questionList = auxilaryProcess(s);

			for (String q : questionList) {
				PatternMatchingResultBean pmRB = this.getCandidatePropName(q, propMap);
				rsBean.add(pmRB);
				System.out.println(
						"string " + q + " has the answer of " + pmRB.getAnswer() + " with score " + pmRB.getScore());
			}

			// propName = this.getCandidatePropName(s, listProp);
		}

		/*
		 * 4. Build the Cypher SQL and get the answer
		 */
		int finalScore = Integer.MIN_VALUE;
		for (PatternMatchingResultBean b : rsBean) {
			if (!b.isEmpty()) {
				System.out.println("candidate " + b.getAnswer() + " has score:" + b.getScore());
				if (b.getScore() > finalScore) {
					propName = b.getAnswer();
					finalScore = b.getScore();
				}
			}
		}

		System.out.println("propName is " + propName);
		System.out.println("rs is " + rs);
		rs = DBProcess.getPropertyValue(ent, propMap.get(propName));

		return rs;
	}

	// get the property set with synonym process
	private Map<String, String> getPropertyNameSet(String ent) {
		Map<String, String> rsMap = new HashMap<>();
		List<String> propList = DBProcess.getPropertyNameSet(ent);
		for (String iProp : propList) {
			rsMap.put(iProp, iProp);
			Set<String> setSyn = NLPProcess.getSynonymWord(iProp);
			for (String iSyn : setSyn) {
				rsMap.put(iSyn, iProp);
			}
		}
		System.out.println("all the prop is: " + rsMap);
		return rsMap;
	}

	/*
	 * get the target entity by Solr
	 */
	private String getEntityBySolr(String question) {
		// TBD: hard code for 3/15
		String ent = "姚明";
		return ent;
	}

	/*
	 * return the part before the entity in the sentence if question does not
	 * contain ent, return null.
	 */
	private Set<String> getPartsWithoutEntity(String str, String ent) {
		Set<String> listPart = new HashSet<>();
		while (str.lastIndexOf(ent) != -1) {
			String s = str.substring(str.lastIndexOf(ent) + ent.length());
			if (!s.isEmpty()){
				System.out.println("in GetParts, str:" + str + " is added into list");
				listPart.add(s); // add the last part
			}
				
			// remove the last part
			str = str.substring(0, str.lastIndexOf(ent));
			System.out.println("in GetParts, str is " + str);
			if (!str.isEmpty() && str.lastIndexOf(ent) == -1) {
				listPart.add(str);
				System.out.println("in GetParts, str:" + str + " is added into list");
			}

		}
		return listPart;
	}

	/*
	 * remove the stopword in a string.
	 */
	private String removeStopWord(String str) {
		String rs = "";
		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		System.out.println("original string is " + str);
		List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			String s = segPos.get(i).word;
			System.out.print(s+", ");
			if (!NLPProcess.isStopWord(s))
				rs += s;
		}
		System.out.println("");

		return rs;
	}

	/*
	 * replace the synonym in a string.
	 */
	private List<String> replaceSynonymProcess(String str) {
		List<String> rsSet = new ArrayList<>();
		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		System.out.println("string is " + str);
		List<Term> segPos = tnNode.getWordPos();
		rsSet.add("");
		for (int i = 0; i < segPos.size(); i++) {
			String iWord = segPos.get(i).word;
			System.out.println("current word is " + iWord);

			Set<String> iSynSet = NLPProcess.getSynonymWord(iWord);
			if (iSynSet.size() > 0) {
				System.out.println("\t has syn: " + iSynSet);
				// if there are synonyms, combine each of them
				List<String> newRS = new ArrayList<>();
				for (String iSyn : iSynSet) {
					// System.out.println("iSyn is " + iSyn);
					List<String> tmpRS = new ArrayList<>();
					tmpRS.addAll(rsSet);
					for (int j = 0; j < tmpRS.size(); j++) {
						tmpRS.set(j, tmpRS.get(j) + iSyn);
					}
					newRS.addAll(tmpRS);
					// System.out.println("tempRS is: " + tmpRS + "; newRS is "
					// + newRS);
				}
				rsSet = newRS;
				System.out.println("after syn is: " + newRS);
			} else {
				System.out.println("\t No syn: " + iWord);
				for (int j = 0; j < rsSet.size(); j++) {
					rsSet.set(j, rsSet.get(j) + iWord);
				}
			}
		}

		// add the original string
		if (rsSet.size() > 1) {
			rsSet.add(str);
		}

		System.out.println("in replaceSyn, input is" + str);
		for (String iRS : rsSet) {
			System.out.println("candidate is: " + iRS);
		}

		return rsSet;
	}

	/*
	 * address the auxiliary words in a string
	 */
	private List<String> auxilaryProcess(String str) {
		/*
		 * 1. remove the auxiliary words like "多少" in a sentence
		 */
		str = this.removeStopWord(str);
		System.out.println("after removing Stop Words: " + str);

		/*
		 * 2. replace the synonyms
		 */
		List<String> rsSet = this.replaceSynonymProcess(str);
		System.out.println("after synonym process: " + rsSet);

		return rsSet;
	}

	/*
	 * return the property with the highest score; return null if the threshold
	 * is hold the version without segPos
	 */
	private PatternMatchingResultBean getCandidatePropName(String str, Map<String, String> prop) {
		// threshold to pass: if str contain a property, pass
		boolean isPass = false;
		HashMap<String, Integer> score = new HashMap<String, Integer>();
		System.out.println("query string is: " + str);

		for (String s : prop.keySet()) {
			System.out.println("current prop is: " + s);

			if (!isPass && str.lastIndexOf(s) != -1) {
				isPass = true;
			}

			// compute the score by scanning from left to right
			String tempS = s;
			int left2right = 0;
			for (int i = 0; i < str.length(); i++) {
				if (tempS.indexOf(str.charAt(i)) == 0) {
					left2right++;
					tempS = tempS.substring(1);
				} else {
					left2right--;
				}
			}

			// compute the score by scanning from right to left
			tempS = s;
			int right2left = 0;
			for (int i = str.length() - 1; i >= 0; i--) {
				System.out.println(tempS+" "+str.charAt(i));
				if (tempS.lastIndexOf(str.charAt(i)) == tempS.length() - 1) {
					right2left++;
					tempS = tempS.substring(0, tempS.length() - 1);
				} else {
					right2left--;
				}
			}

			if (left2right > right2left) {
				score.put(s, left2right);
			} else {
				score.put(s, right2left);
			}

			System.out.println("left is " + left2right + ". right is " + right2left + ". score is " + score.get(s));
		}

		int finalScore = Integer.MIN_VALUE;
		PatternMatchingResultBean rs = new PatternMatchingResultBean();
		for (String s : prop.keySet()) {
			if (score.get(s) > finalScore) {
				finalScore = score.get(s);
				rs.setAnswer(s);
				rs.setScore(finalScore);
			}
		}
		System.out.println("finalScore is " + finalScore + ". rs is " + rs);

		if (finalScore >= 0 || isPass) {
			if (finalScore < 0)
				System.out.println("return when score<0, case is " + str + ", return prop is " + rs);
			return rs;
		} else
			return null;
	}

	public static void main(String[] args) {
		PatternMatchingProcess mp = new PatternMatchingProcess();
		String str = "姚明打的位置是什么？";
		String ent = "姚明";

		mp.getAnswer(str);
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
