package com.emotibot.patternmatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		/*
		 * 1. get the entity by Solr
		 */
		String ent = getEntityBySolr(question);

		/*
		 * 2. split the sentence by the entity and get two sub parts: the
		 * previous and the latter one 2.1 remove the auxiliary words like "多少"
		 */
		// get the last one for 3/15, may improve latter
		String postStr = this.getPartsWithoutEntity(question, ent).get(0);		
		System.out.println("postStr is " + postStr);
		
		postStr = auxilaryProcess(postStr);
		System.out.println("aux process is " + postStr);

		/*
		 * 3. compute the score and get the candidate
		 */
		List<String> listProp = DBProcess.getPropertyNameSet(ent);
		String propName = this.getCandidatePropName(postStr, listProp);
		System.out.println("propName is " + propName);

		/*
		 * 4. Build the Cypher SQL and get the answer
		 */
		String rs = DBProcess.getPropertyValue(ent, propName);
		System.out.println("rs is " + rs);
		return rs;
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
	private List<String> getPartsWithoutEntity(String question, String ent) {
		List<String> listPart = new ArrayList<>();
		while (question.lastIndexOf(ent) != -1) {
			String s = question.substring(question.lastIndexOf(ent) + ent.length());
			if (!s.isEmpty())
				listPart.add(s); // add the last part
			// remove the last part
			question = question.substring(0, question.lastIndexOf(ent));
			if (question.lastIndexOf(ent) == -1)
				listPart.add(question);
		}
		return listPart;
	}

	/*
	 * get the synonym of the word from the syn table
	 */
	private String synonymProcess(String str) {
		NLPResult tnNodeSy = NLPSevice.ProcessSentence(str, NLPFlag.Synonyms.getValue());
		List<List<String>> sy = tnNodeSy.getSynonyms();
		System.out.println("size of synonym is " + sy.size());
		String rs = "";
		if (sy.size() > 0) {
			System.out.println("size of synonym is " + sy.size());
			rs = sy.get(0).get(0);
		}
		return rs;
	}
	
	/*
	 * replace the synonym in a sentence or a word
	 */
	private String removeStopWord(String sentence) {
		NLPResult tnNode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPosNoStopWords.getValue());
		List<Term> segPos = tnNode.getWordPos();
		String rs = "";
		for (int i = 0; i < segPos.size(); i++) {
			rs += segPos.get(i).word;
		}
		return rs;
	}

	/*
	 * 1. remove the auxiliary words like "多少" in a sentence 2. replace the
	 * synonyms
	 */
	private String auxilaryProcess(String str) {
		/*
		 * 1. remove the auxiliary words like "多少" in a sentence
		 */
		/*for (String s : auxList) {
			while (str.lastIndexOf(s) != -1) {
				str = str.substring(0, str.lastIndexOf(s)) + str.substring(str.lastIndexOf(s) + s.length());
			}
		}*/
		str = this.removeStopWord(str);
		System.out.println("after removing Stop Words: " + str);

		/*
		 * 2. replace the synonyms
		 */
		str = SynonymProcess.synonymProcess(str);
		System.out.println("after synonym process: " + str);

		return str;
	}

	/*
	 * return the property with the highest score; return null if the threshold
	 * is hold the version without segPos
	 */
	private String getCandidatePropName(String str, List<String> prop) {
		// threshold to pass: if str contain a property, pass
		boolean isPass = false;
		HashMap<String, Integer> score = new HashMap<String, Integer>();
		System.out.println("query string is: " + str);

		for (String s : prop) {
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
		String rs = "";
		for (String s : prop) {
			if (score.get(s) > finalScore) {
				finalScore = score.get(s);
				rs = s;
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
		String question = "你知道姚明单场最高多少分吗";
		String ent = "姚明";
		
		
		System.out.println("test aux:"+mp.getAnswer("姚明的妻子是谁？"));
		
		
/*
		System.out.println(mp.getAnswer(question));

		System.out.println(ent.charAt(0) + "  " + ent.charAt(ent.length() - 1) + " index of " + ent.lastIndexOf("明")
				+ " length=" + ent.length());

		System.out.println("case is " + question);

		// test for getPartsWithoutEntity
		System.out.println("the position is " + question.lastIndexOf(ent) + " and lenth is " + question.length()
				+ " ent lenth is " + ent.length());
		int pos = question.lastIndexOf(ent);
		System.out.println("the pre is " + question.substring(0, pos) + ". And the post is "
				+ question.substring(pos + ent.length()));

		for (String s : mp.getPartsWithoutEntity(question, ent)) {
			System.out.println(s);
		}

		question = mp.getPartsWithoutEntity(question, ent).get(0);
		System.out.println("after trancate: " + question);

		// test for auxilary process
		question = mp.auxilaryProcess(question);

		// test for Match Score
		List<String> listProp = DBProcess.getPropertyNameSet(ent);
		System.out.println("prop name is " + listProp);
		String propName = mp.getCandidatePropName(question, listProp);
*/
	}

}
