package com.emotibot.patternmatching;

import java.util.List;

import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.hankcs.hanlp.seg.common.Term;

public class SynonymProcess {
	
	/*
	 * replace the synonym in a sentence or a word
	 */
	public static String stopWordList(String sentence) {
		NLPResult tnNode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPosNoStopWords.getValue());
		List<Term> segPos = tnNode.getWordPos();
		System.out.println("test="+segPos);
		String rs = "";
		for (int i = 0; i < segPos.size(); i++) {
			String word = segPos.get(i).word;
			System.out.println("index " + i + " is " + word + " " + segPos.get(i).nature.toString());
		}
		return rs;
	}

	/*
	 * replace the synonym in a sentence or a word
	 */
	public static String synonymProcess(String sentence) {
		NLPResult tnNode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
		String rs = "";
		for (int i = 0; i < segPos.size(); i++) {
			String word = segPos.get(i).word;
			System.out.println("index " + i + " is " + word + " " + segPos.get(i).nature.toString());
			String syn = getSynonym(word);
			if (syn.isEmpty()) {
				rs += word;
				System.out.println("orginal is " + word + "; syn is " + syn);
			} else {
				rs += syn;
				System.out.println("orginal is " + word + "; syn is " + syn);
			}
		}
		return rs;
	}

	/*
	 * get the synonym of the word from the syn table
	 */
	private static String getSynonym(String str) {
		NLPResult tnNodeSy = NLPSevice.ProcessSentence(str, NLPFlag.Synonyms.getValue());
		List<List<String>> sy = tnNodeSy.getSynonyms();
		System.out.println("size of synonym is " + sy.size());
		String rs = "";
		if (sy.size() > 0 && sy.get(0).size()>0) {
			rs = sy.get(0).get(0);
		}
		return rs;
	}

	public static void main(String[] args) {
		SynonymProcess sp = new SynonymProcess();
		// sp.synonymProcess("");
		String s = sp.synonymProcess("姚明的身高是多少");
		System.out.println("rs is " + s);

	}
}
