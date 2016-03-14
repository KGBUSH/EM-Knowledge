package com.emotibot.nlpparser;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: yunzhou@emotibot.com.cn
 */
import java.util.ArrayList;
import java.util.List;

import org.neo4j.cypher.internal.compiler.v2_2.perty.recipe.Pretty.listAppender;

import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPSevice;
import com.hankcs.hanlp.seg.common.Term;

public class Pre_ProcessSentence {
	public static NLPSevice nlps = new NLPSevice();

	/**
	 * 分词
	 * 
	 * @param sentence
	 * @return
	 */
	public static List<Term> getSegPos(String sentence) {
		List<Term> segPos = nlps.ProcessSentence(sentence, NLPFlag.SegPos.getValue()).getWordPos();

		return segPos;
	}

	public static void main(String[] args) {
		System.out.println(Pre_ProcessSentence.getSegPos("姚明所属运动队，地的出生地"));
	}

}
