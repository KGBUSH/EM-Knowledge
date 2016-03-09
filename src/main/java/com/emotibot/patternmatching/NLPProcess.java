package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class NLPProcess {
	private static HashMap<String, Set<String>> synonymTable = createSynonymTable();
	private static HashMap<String, List<String>> synonymTableRef = createSynonymTableRef();
	private static Set<String> stopWordTable = createStopWordTable();

	private static Set<String> createStopWordTable() {
		Set<String> stopWordSet = new HashSet<>();
		String fileName = "txt/stopwords.txt";
		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String word = "";
				while ((word = dis.readLine()) != null) {
					stopWordSet.add(word.trim());
				}
				// System.out.println("list is " + stopWordSet);

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return stopWordSet;
	}

	public static boolean isStopWord(String str) {
		boolean isSW = false;
		if (!str.isEmpty() && stopWordTable.contains(str)) {
			isSW = true;
		}

		return isSW;
	}

	private static HashMap<String, List<String>> createSynonymTableRef() {
		HashMap<String, List<String>> syn = new HashMap<>();
		String fileName = "txt/SynonymNoun.txt";
		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String line = "";
				// int i = 0;

				while ((line = dis.readLine()) != null) {
					if (line.lastIndexOf("=") != -1) {
						String[] words = line.trim().split(" ");
						String id = words[0].substring(0, words[0].length() - 1);
						List<String> setElementSyn = new ArrayList<>();

						for (int j = 1; j < words.length; j++) {
							setElementSyn.add(words[j]);
						}
						syn.put(id, setElementSyn);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return syn;
	}

	private static HashMap<String, Set<String>> createSynonymTable() {
		HashMap<String, Set<String>> syn = new HashMap<>();
		String fileName = "txt/SynonymNoun.txt";
		if (!Tool.isStrEmptyOrNull(fileName)) {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String line = "";
				// int i = 0;

				while ((line = dis.readLine()) != null) {
					if (line.lastIndexOf("=") != -1) {
						String[] words = line.trim().split(" ");
						String id = words[0].substring(0, words[0].length() - 1);
						// System.out.println("id=" + id);
						for (int j = 1; j < words.length; j++) {
							Set<String> ss = null;
							if (syn.containsKey(words[j])) {
								ss = syn.get(words[j]);
							} else {
								ss = new HashSet<String>();
							}
							ss.add(id);
							syn.put(words[j], ss);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return syn;
	}

	// return all the first words of synonym
	public static Set<String> getSynonymWord(String str) {
		Set<String> synSet = new HashSet<>();
		Set<String> synWord = new HashSet<>();

		if (!str.isEmpty() && synonymTable.containsKey(str)) {
			synSet = synonymTable.get(str);

			for (String s : synSet) {
				if (!synonymTableRef.containsKey(s))
					System.out.println("@@@@@@@@@@ conflict in Synonym Table");
				synWord.add(synonymTableRef.get(s).get(0));
			}
		}

		return synWord;
	}

	public static void fun() {
		System.out.println("in fun");
	}

	/*
	 * replace the synonym in a sentence or a word
	 */
	public static String stopWordList(String sentence) {
		NLPResult tnNode = NLPSevice.ProcessSentence(sentence, NLPFlag.SegPosNoStopWords.getValue());
		List<Term> segPos = tnNode.getWordPos();
		System.out.println("test=" + segPos);
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
		if (sy.size() > 0 && sy.get(0).size() > 0) {
			rs = sy.get(0).get(0);
		}
		return rs;
	}

	public static void main(String[] args) {
		NLPProcess sp = new NLPProcess();
		// sp.synonymProcess("");
		System.out.println("syn is "+sp.getSynonymWord("伯"));

	}
}
