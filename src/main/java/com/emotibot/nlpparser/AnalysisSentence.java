package com.emotibot.nlpparser;

import java.util.ArrayList;
import java.util.List;

import com.hankcs.hanlp.seg.common.Term;

public class AnalysisSentence {
	/**
	 * get sentence attribute
	 * 
	 * @param org
	 * @return
	 */
	public static List<Name_Type> getAttribute(List<Term> org) {
		List<Name_Type> attribute = new ArrayList<Name_Type>();
		for (int i = 0; i < org.size(); i++) {
			String poString = org.get(i).nature.toString();

			if (poString.equals("ude2") && !org.get(i).word.equals("地")) {
				String type = "2";
				Name_Type nType = new Name_Type(org.get(i), type);
				attribute.add(nType);
			}
		}
		return attribute;
	}

	public static List<Name_Type> getEntity(List<Term> org) {
		List<Name_Type> entity = new ArrayList<Name_Type>();
		for (int i = 0; i < org.size(); i++) {
			String poString = org.get(i).nature.toString();

			if (poString.equals("nr")) {
				String type = "1";
				Name_Type nType = new Name_Type(org.get(i), type);
				entity.add(nType);
			}
		}
		return entity;
	}

	
	/**
	 * sentence to Struct
	 * 
	 * @param sentence
	 */
	public static String analysisSentenceToGetAnswer(String sentence) {
		String answer = "";
		List<Term> segpos = Pre_ProcessSentence.getSegPos(sentence);
		System.out.println(segpos);
		List<Name_Type> entity = getEntity(segpos);
		System.out.println(entity);
		List<Name_Type> attribute = getAttribute(segpos);
		System.out.println(attribute);
		answer = TraversalToGraph.traversal(entity, attribute);
		return answer;
	}

	public static void main(String[] args) {
		String sentence = "姚明所属运动队？";
		AnalysisSentence.analysisSentenceToGetAnswer(sentence);

	}
}
