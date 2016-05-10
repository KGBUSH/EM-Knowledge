package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.List;

import com.emotibot.log.LogService;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class NLPUtil {


	// return the segPos by Hanlp method
	public static List<Term> getSegWord(String sentence) {
		List<Term> segWord = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(sentence)) {
			return segWord;
		}
		segWord = HanLP.segment(sentence);
		return segWord;
	}
	


	// input: 欧洲
	// output: <欧罗巴,欧罗巴洲>
	public static List<String> getSynonymnEntityList(String dbEntity) {
		List<String> list = new ArrayList<>();
		if (!Tool.isStrEmptyOrNull(dbEntity)) {
			list = DictionaryBuilder.getEntitySynonymReverseTable().get(dbEntity);
			if (list == null || list.isEmpty()) {
				System.err.println("NLPProcess.getSynonymnEntityList" + "input=" + dbEntity);
				LogService.printLog("0", "NLPProcess.getSynonymnEntityList", "input=" + dbEntity);
			}
			return list;
		}
		return list;
	}
	
}
