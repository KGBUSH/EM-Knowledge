package com.emotibot.template;

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
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class TemplateProcessor extends AbstractAIMLEngine {

	public TemplateProcessor(String str) {
		super(str);
	}

	

	public String selectiveQuestionProcess(String sentence) {
		String processedQ = Tool.insertSpace2Chinese(sentence);
		String type = chatSession.multisentenceRespond(processedQ);
		System.out.println("Sentence = " + sentence + "\n ProcessQ = " + processedQ + "\n type 1 = " + type);
		type = Tool.removeSpaceFromChinese(type);
		System.out.println("type 2 = " + type);
		return type;
	}

	public String process(String sentence) {
		System.out.println("s = "+sentence);
		String processedQ;
		// NLPResult tnode = NLPSevice.ProcessSentence(sentence,
		// NLPFlag.all.getValue());
		// List<Term> segword = tnode.getWordPos();
		// CoNLLSentence resetence = tnode.getReCoNLLSentence();
		// CoNLLWord[] reCoNLLWords = resetence.word;
		// for (CoNLLWord word : reCoNLLWords) {
		// System.out.print("LEMMA:" + word.LEMMA + " NAME:" + word.NAME + "
		// POSTAG:" + word.POSTAG + " LEVEL:"
		// + word.HEAD.ID + " HEAD:" + word.HEAD.LEMMA + "\n");
		// }
		// StringBuilder ss = new StringBuilder();
		// for (Term t : segword) {
		// if (t.nature.toString().startsWith("n")) {
		// ss.append("##");
		// ss.append(" <pos>n</pos>");
		// }
		// ss.append(t.word);

		// ss.append(" <pos>");
		// if (t.nature.toString().startsWith("a"))
		// ss.append("a");
		// else if (t.nature.toString().startsWith("n"))
		// ss.append("n");
		// else if (t.nature.toString().startsWith("v"))
		// ss.append("v");
		// else
		// ss.append(t.nature.toString());
		// ss.append("</pos> ");
		// }

		processedQ = Tool.insertSpace2Chinese(sentence);
		String type = chatSession.multisentenceRespond(processedQ);
		System.out.println("Sentence = " + sentence + "\n ProcessQ = " + processedQ + "\n type 1 = " + type);

		type = Tool.removeSpaceFromChinese(type);
		System.out.println("type 2 = " + type);
		return type;
	}
	
	public String processQuestionClassifier(String sentence) {
		System.out.println("s = "+sentence);
		String processedQ;

		processedQ = Tool.insertSpace2Sentence(sentence);
		String type = chatSession.multisentenceRespond(processedQ);
		System.out.println("Sentence = " + sentence + "\n ProcessQ = " + processedQ + "\n type 1 = " + type);

		type = Tool.removeSpaceFromChinese(type);
		System.out.println("type 2 = " + type);
		return type;
	}


	public static void main(String[] args) {

		long timeCounter = System.currentTimeMillis();
//		String sen = "## lady gaga  <type>entity</type><label>figure</label> 多重";
		String sen = "##  <type>entity</type> 是 谁"; 
//		String sen = "说  说  ## 姚 明 <type>entity</type>"; 
//		String sen = "说 说 ## 姚 明 <type>entity</type><label>figure</label>"; 
//		String sen = "刘 翔 退 役 刘 翔 多 久 刘  多 久 役 多 久 退 役 刘 多 久 役  多 久 对 ";
		
//		TemplateProcessor implicationTemplate = new TemplateProcessor("QuestionClassifier");
//		for(int i=0;i<20;i++){
//			sen = sen.substring(0, sen.length()-1);
//			System.out.println(implicationTemplate.processQuestionClassifier(sen));
//		}
//		System.out.println("TIME 1 - before get entity >>>>>>>>>>>>>> " + (System.currentTimeMillis() - timeCounter));
//		System.exit(0);
		
		
//		sen = "北京的行政代码是不是110000";
		
//		System.exit(0);
		String str = sen;
		TemplateProcessor introTemplate = new TemplateProcessor("QuestionClassifier");
		System.out.println("\n\n processTest====");
		System.out.println("process="+introTemplate.process(str));
		
		TemplateProcessor tt = new TemplateProcessor("QuestionClassifier");
		
		str = sen;
		System.out.println("str="+str);
		System.out.println("\n process====");
		System.out.println(introTemplate.processQuestionClassifier(str));

	}

}
