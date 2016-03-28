package com.emotibot.patternmatching;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class NLPProcess {
	private static HashMap<String, Set<String>> synonymTable = createSynonymTable();
	private static HashMap<String, List<String>> synonymTableRef = createSynonymTableRef();
	private static Set<String> stopWordTable = createStopWordTable();
	private static Set<String> entityTable = createEntityTable();

	// create stopword table Set
	private static Set<String> createEntityTable() {
		Set<String> entitySet = new HashSet<>();
		String filePath = Common.UserDir + "/knowledgedata/domain";
		System.out.println("path is " + filePath);

		if (!Tool.isStrEmptyOrNull(filePath)) {
			try {
				// BytesEncodingDetect s = new BytesEncodingDetect();
				// String fileCode =
				// BytesEncodingDetect.nicename[s.detectEncoding(new
				// File(filePath))];
				// if (fileCode.startsWith("GB") && fileCode.contains("2312"))
				// fileCode = "GB2312";
				File fileDictoray = new File(filePath);
				File[] allFile = fileDictoray.listFiles();
				for (File f : allFile) {
					FileInputStream fis = new FileInputStream(f);
					InputStreamReader read = new InputStreamReader(fis);
					BufferedReader dis = new BufferedReader(read);
					String word = "";
					while ((word = dis.readLine()) != null) {
						if (!word.trim().isEmpty())
							entitySet.add(word.trim());
					}
					dis.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return entitySet;
	}

	// create stopword table Set
	private static Set<String> createStopWordTable() {
		Set<String> stopWordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/stopwords.txt";
		// "txt/stopwords.txt";
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
				dis.close();
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

	// create synonym reference hash map table: Map<id, List of Synonym>
	private static HashMap<String, List<String>> createSynonymTableRef() {
		HashMap<String, List<String>> syn = new HashMap<>();
		String fileName = Common.UserDir + "/knowledgedata/SynonymNoun.txt";
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
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return syn;
	}

	// create synonym hash map table Map<words, id>
	private static HashMap<String, Set<String>> createSynonymTable() {
		HashMap<String, Set<String>> syn = new HashMap<>();
		String fileName = Common.UserDir + "/knowledgedata/SynonymNoun.txt";
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

	// Get the set of the first word in the line which contains input word in
	// synonym dictionary
	// input: "标志"
	// output: ["标志"，"记号"]
	public static Set<String> getSynonymWordSet(String str) {
		Set<String> synSet = new HashSet<>();
		Set<String> synWord = new HashSet<>();
		if (Tool.isStrEmptyOrNull(str)) {
			System.err.println("NLPProcess.getSynonymWordSet: input is empty");
			return synWord;
		}

		if (!str.isEmpty() && synonymTable.containsKey(str)) {
			synSet = synonymTable.get(str);

			for (String s : synSet) {
				if (!synonymTableRef.containsKey(s))
					System.out.println("@@@@@@@@@@ conflict in Synonym Table");
				synWord.add(synonymTableRef.get(s).get(0));
			}
		}

		// System.out.println("pattern matching: NLPProcess.getSynnoymWordSet
		// input = "+str+", output="+synWord);

		return synWord;
	}

	// get the property set in DB with synonym process
	// return Map<synProp, prop>
	private static Map<String, String> getPropertyNameSet(String ent) {
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

	// Get the matched property in DB
	// return "" if the input prop and its synonyms do not match any existing
	// property in DB
	public static String matchSynonymPropertyInDB(String entity, String prop) {
		System.out.println("input is entity:" + entity + " prop:" + prop);
		List<String> rsList = new ArrayList<>();
		Map<String, String> propMap = getPropertyNameSet(entity);
		Set<String> synSet = getSynonymWordSet(prop);
		System.out.println("get synonym set of input prop: " + synSet + " size is " + synSet.size());
		System.out.println("get prop set in DB: " + propMap);

		for (String q : synSet) {
			if (propMap.containsKey(q) && !rsList.contains(propMap.get(q))) {
				rsList.add(propMap.get(q));
			}
		}
		System.out.println("getSynonymProperty in NLPProcess: rs is " + rsList);

		if (rsList.isEmpty())
			return "";
		else
			return rsList.get(0);
	}

	// return the set of entity which is contained in the input sentence
	// input: 姚明和叶莉的女儿是谁？
	// output: [姚明，叶莉]
	public static List<String> getEntitySimpleMatch(String str) {
		List<String> entitySet = new ArrayList<>();
		for (String s : entityTable) {
			if (str.contains(s)) {
				entitySet.add(s);
			}
		}

		System.out.println("the mached entities are: " + entitySet.toString());
		return entitySet;
	}

	public static void main(String[] args) {
		NLPProcess sp = new NLPProcess();
		// sp.synonymProcess("");

		String str = "香港金像奖有没有上海大学";
		List<String> es = getEntitySimpleMatch(str);
		for (String s : es) {
			System.out.println(s.length());
		}
		
		System.out.print("size of entity table is "+entityTable.size());

		String fileEntity = Common.UserDir + "/knowledgedata/entity.txt";

		try {
			File writename = new File(fileEntity);
			writename.createNewFile(); // 创建新文件
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			for (String s : entityTable) {
//				System.out.println(s.length());
				out.write(s + "\r\n"); // \r\n即为换行
			}
			out.flush(); // 把缓存区内容压入文件
			out.close(); // 最后记得关闭文件
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("syn is " + matchSynonymPropertyInDB("姚明", "女人"));

		// System.out.println("syn is " + sp.getSynonymWord("伯"));

	}
}
