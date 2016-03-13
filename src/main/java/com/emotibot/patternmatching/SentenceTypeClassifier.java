package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * copy from Yongning's template module (ruleEngine) with small modificaiton by Quan Zu
 */
	
import java.util.List;

import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.hankcs.hanlp.seg.common.Term;

public class SentenceTypeClassifier extends AbstractAIMLEngine {

	public SentenceTypeClassifier() {
		super("Knowledge");
	}

	private String removeSpace(String str) {
		String[] splitStr = str.split(" ");
		String rs = "";
		for (String s : splitStr) {
			rs += s;
		}

//		System.out.println("input is " + str + ", output is " + rs);
		return rs;
	}

	public String getSentenceType(String sentence) {
		String processedQ;
//		NLPResult tnode = NLPSevice.ProcessSentence(sentence, NLPFlag.all.getValue());
//		List<Term> segword = tnode.getWordPos();
//		CoNLLSentence resetence = tnode.getReCoNLLSentence();
//		CoNLLWord[] reCoNLLWords = resetence.word;
//		for (CoNLLWord word : reCoNLLWords) {
//			System.out.print("LEMMA:" + word.LEMMA + " NAME:" + word.NAME + "		 POSTAG:" + word.POSTAG + " LEVEL:"
//					+ word.HEAD.ID + " HEAD:" + word.HEAD.LEMMA + "\n");
//		}
//		StringBuilder ss = new StringBuilder();
//		for (Term t : segword) {
//			if (t.nature.toString().startsWith("n")) {
//				ss.append("##");
//				ss.append(" <pos>n</pos>");
//			}
//			ss.append(t.word);
			
//			ss.append(" <pos>");						
//			if (t.nature.toString().startsWith("a"))
//				ss.append("a");
//			else if (t.nature.toString().startsWith("n"))
//				ss.append("n");
//			else if (t.nature.toString().startsWith("v"))
//				ss.append("v");
//			else
//				ss.append(t.nature.toString());			
//			ss.append("</pos> ");	
//		}
		
		processedQ = insertSpace2Chinese(sentence);
		String type = chatSession.multisentenceRespond(processedQ);
		System.out.println("Sentence = "+sentence+"\n ProcessQ = "+processedQ+"\n type 1 = "+type);
		
		type = removeSpace(type);
		System.out.println("type 2 = "+type);
		return type;
	}

	public static void main(String[] args) {
		SentenceTypeClassifier sentenceTypeClassifier = new SentenceTypeClassifier();
		String str = "姚明多重";
		System.out.println("===="+sentenceTypeClassifier.getSentenceType(str));
		
		
//		String str1 = "## 姚 明 <pos>nr</pos> 属 什 么";
//		String str2 = "## 姚 明 <type>entity</type> 属 什 么";
//		System.out.println("@@@@==="+sentenceTypeClassifier.chatSession.multisentenceRespond(str2));
		
//		System.out.println("1=" + sentenceTypeClassifier.getSentenceType("姚明有多重"));
//		System.out.println("1=" + sentenceTypeClassifier.getSentenceType("## 姚 明 <pos>n</pos> 属 什 么"));
		//		System.out.println("3=" + sentenceTypeClassifier.getSentenceType("姚明属什么"));
		// System.out.println("1=" +
		// sentenceTypeClassifier.getSentenceType("姚明有多重呢"));
		// System.out.println("2=" +
		// sentenceTypeClassifier.getSentenceType("姚明多重"));
		// System.out.println("2=" +
		// sentenceTypeClassifier.getSentenceType("姚明多重呢"));
	}

}