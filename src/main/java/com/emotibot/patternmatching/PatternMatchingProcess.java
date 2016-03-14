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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.hankcs.hanlp.seg.common.Term;

public class PatternMatchingProcess {

	final SentenceTypeClassifier sentenceClassifier = new SentenceTypeClassifier();

	/*
	 * Get the answer by the match score method input: the question sentence
	 * from users output: the answer without answer rewriting
	 */
	public String getAnswer(String sentence) {

		String rs = "";
		/*
		 * 1. get the entity by Solr and Revise by template
		 */
		String entity = getEntityBySolr(sentence);
		sentence = templateProcess(entity, sentence);

		/*
		 * 2. split the sentence by the entities to get candidates
		 */
		Set<String> candidateSet = this.getCandidateSet(sentence, entity);
		System.out.println("all candidates are " + candidateSet);
		Map<String, String> propMap = this.getPropertyNameSet(entity);

		// 3. compute the score for each candidate
		List<PatternMatchingResultBean> rsBean = new ArrayList();
		for (String s : candidateSet) {
			System.out.println("Candidate is " + s);
			// remove stopWord and get all possible candidates w.r.t. synonyms
			List<String> synList = auxilaryProcess(s);
			System.out.println("all syn questions are " + synList);

			for (String q : synList) {
				PatternMatchingResultBean pmRB = this.getCandidatePropName(q, propMap);
				if (!pmRB.isEmpty()) {
					rsBean.add(pmRB);
					System.out.println("string " + q + " has the answer of " + pmRB.getAnswer() + " with score "
							+ pmRB.getScore());
				}
			}
		}

		/*
		 * 4. Build the Cypher SQL and get the answer
		 */
		int finalScore = Integer.MIN_VALUE;
		String propName = "";
		for (PatternMatchingResultBean b : rsBean) {
			if (!b.isEmpty()) {
				System.out.println("candidate " + b.getAnswer() + " has score:" + b.getScore());
				if (b.getScore() > finalScore) {
					propName = b.getAnswer();
					finalScore = b.getScore();
				}
			}
		}

		if (propName.isEmpty()) {
			propName = "firstParamInfo";
		}
		System.out.println("propName is " + propName);
		rs = DBProcess.getPropertyValue(entity, propMap.get(propName));
		System.out.println("rs is " + rs);

		return rs;
	}

	// get the property set in DB with synonym process
	// return Map<synProp, prop>
	private Map<String, String> getPropertyNameSet(String ent) {
		Map<String, String> rsMap = new HashMap<>();
		List<String> propList = DBProcess.getPropertyNameSet(ent);
		for (String iProp : propList) {
			rsMap.put(iProp, iProp);
			Set<String> setSyn = NLPProcess.getSynonymWordSet(iProp);
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
	 * split the sentence by the entities to get candidates. if question does
	 * not contain ent, return null.
	 */
	private Set<String> getCandidateSet(String str, String ent) {
		Set<String> listPart = new HashSet<>();
		while (str.lastIndexOf(ent) != -1) {
			String s = str.substring(str.lastIndexOf(ent) + ent.length());
			if (!s.isEmpty()) {
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
		// Segmentation Process
		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		System.out.println("original string is " + str);
		List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			String s = segPos.get(i).word;
			System.out.print(s + ", ");
			if (!NLPProcess.isStopWord(s))
				rs += s;
		}
		System.out.println("");

		return rs;
	}

	/*
	 * generate all the possibility candidates according to synonyms
	 */
	private List<String> replaceSynonymProcess(String str) {
		System.out.println("input of replaceSynonymProcess is " + str);
		List<String> rsSet = new ArrayList<>();
		if (str.isEmpty()) {
			System.out.println("output of replaceSynonymProcess is " + rsSet);
			return rsSet;
		}

		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
		rsSet.add("");
		boolean flag = false;
		for (int i = 0; i < segPos.size(); i++) {
			String iWord = segPos.get(i).word;
			System.out.println("current word is " + iWord);

			Set<String> iSynSet = NLPProcess.getSynonymWordSet(iWord);
			if (iSynSet.size() > 0) {
				flag = true;
				System.out.println("\t has syn: " + iSynSet);
				// if there are synonyms, combine each of them
				List<String> newRS = new ArrayList<>();
				for (String iSyn : iSynSet) {
					System.out.println("iSyn is " + iSyn);
					List<String> tmpRS = new ArrayList<>();
					tmpRS.addAll(rsSet);
					for (int j = 0; j < tmpRS.size(); j++) {
						tmpRS.set(j, tmpRS.get(j) + iSyn);
					}
					newRS.addAll(tmpRS);
					System.out.println("tempRS is: " + tmpRS + "; newRS is " + newRS);
				}
				rsSet = newRS;
				// System.out.println("after syn is: " + newRS);
			} else {
				// System.out.println("\t No syn: " + iWord);
				for (int j = 0; j < rsSet.size(); j++) {
					rsSet.set(j, rsSet.get(j) + iWord);
				}
			}
		}

		// add the original string
		if (flag) {
			rsSet.add(str);
		}

		return rsSet;
	}

	/*
	 * remove stopWord and get all possibilities of the candidate with respect
	 * to different synonyms
	 */
	private List<String> auxilaryProcess(String str) {
		/*
		 * 1. remove the stop words like "多少" in a sentence
		 */
		str = this.removeStopWord(str);
		System.out.println("after removing Stop Words: " + str);

		/*
		 * 2. generate candidates according to the synonyms
		 */
		List<String> rsSet = this.replaceSynonymProcess(str);
		System.out.println("after synonym process: " + rsSet + " size is " + rsSet.size());

		return rsSet;
	}

	/*
	 * return the property with the highest score; return null if the threshold
	 * is hold the version without segPos
	 */
	private PatternMatchingResultBean getCandidatePropName(String str, Map<String, String> prop) {
		// threshold to pass: if str contain a property in DB, pass
		boolean isPass = false;
		HashMap<String, Integer> score = new HashMap<String, Integer>();
		System.out.println("query string is: " + str);

		for (String s : prop.keySet()) {
			// System.out.println("current prop is: " + s);

			if (!isPass && str.lastIndexOf(s) != -1) {
				isPass = true;
			}

			// pattern matching algorithm suggested by Phantom
			// compute the score by scanning from left to right
			String tmpProp = s;
			int left2right = 0;
			for (int i = 0; i < str.length(); i++) {
				if (tmpProp.indexOf(str.charAt(i)) == 0) {
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

			// extend the algorithm by adding the process from right to left
			// compute the score by scanning from right to left
			tmpProp = s;
			int right2left = 0;
			for (int i = str.length() - 1; i >= 0; i--) {
				// System.out.println(tmpProp + " " + str.charAt(i));
				if (!tmpProp.isEmpty() && tmpProp.lastIndexOf(str.charAt(i)) == tmpProp.length() - 1) {
					right2left++;
					tmpProp = tmpProp.substring(0, tmpProp.length() - 1);
				} else {
					right2left--;
				}
			}
			// System.out.println("right is " + right2left + " isPass is " +
			// isPass);

			if (left2right > right2left) {
				score.put(s, left2right);
			} else {
				score.put(s, right2left);
			}

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
		System.out.println("finalScore is " + finalScore + ". rs is " + rs.toString());
		return rs;
	}

	private boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	private String insertSpace2Chinese(String s) {
		int tail = 0;
		char[] temp = new char[s.length() * 2];
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isChinese(c)) {
				temp[tail] = c;
				temp[tail + 1] = ' ';
				tail += 2;
			} else {
				temp[tail] = c;
				tail++;
			}
		}
		return new String(temp);
	}

	// tempalte process, match and change the exception case to normal case
	private String templateProcess(String entity, String sentence) {
		if (sentence.lastIndexOf(entity) == -1) {
			return sentence;
		}

		String[] strArr = sentence.split(entity);
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
		rs = sentenceClassifier.getSentenceType(rs);
		if (rs.isEmpty()) {
			rs = sentence;
		}

		System.out.println("input=" + sentence + ", output=" + rs);
		return rs;
	}

	public static void main(String[] args) {
		PatternMatchingProcess mp = new PatternMatchingProcess();
		String str = "姚明属什么";

		mp.getAnswer(str);

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
