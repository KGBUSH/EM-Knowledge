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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.nlp.NLPFlag;
import com.emotibot.nlp.NLPResult;
import com.emotibot.nlp.NLPSevice;
import com.emotibot.util.Tool;
import com.emotibot.util.StringLengthComparator;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.common.Term;

public class NLPProcess {
	private static HashMap<String, Set<String>> synonymTable = createSynonymTable();;
	private static HashMap<String, List<String>> synonymTableRef = createSynonymTableRef();
	private static Set<String> stopWordTable = createStopWordTable();
	private static Set<String> entityPMTable = createEntityPMTable();
	private static Set<String> entityTable = createEntityTable();
	// entitySynonymTable:[甲肝，甲型病毒性肝炎]
	private static Map<String, String> entitySynonymTable = createEntitySynonymTable();
	// entitySynonymReverseTable:[甲肝，甲型病毒性肝炎]
	// private static Map<String, String> entitySynonymReverseTable =
	// createEntitySynonymReverseTable();

	public static void NLPProcessInit() {
		addCustomDictionaryInHanlp();
		// synonymTable = createSynonymTable();
		// synonymTableRef = createSynonymTableRef();
		// stopWordTable = createStopWordTable();
		// entityTable = createEntityTable();
		// entitySynonymTable = createEntitySynonymTable();
	}
	
	public static Set<String> getEntityTable(){
		return entityTable;
	}

	private static void addCustomDictionaryInHanlp() {
		for (String s : entityTable) {
			CustomDictionary.add(s);
		}
		for (String s : entitySynonymTable.keySet()) {
			CustomDictionary.add(s);
		}
		for (String s : entitySynonymTable.values()) {
			CustomDictionary.add(s);
		}
	}

	// return the segPos by Hanlp method
	public static List<Term> getSegWord(String sentence) {
		List<Term> segWord = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(sentence)) {
			return segWord;
		}
		segWord = HanLP.segment(sentence);
		return segWord;
	}

	public static Map<String, String> getEntitySynonymTable() {
		return entitySynonymTable;
	}

	// create entity table Set
	// ["甲型病毒性肝炎"，“甲肝”]
	private static Map<String, String> createEntitySynonymTable() {
		String fileName = Common.UserDir + "/knowledgedata/entitySynonym.txt";
		System.out.println("path is " + fileName);
		Map<String, String> entitySyn = new HashMap<>();

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
					String[] wordList = line.trim().split("##");
					if (wordList.length != 2) {
						System.err.println("wrong format in entitySynonym.txt");
						continue;
					}
					// address the case 曼彻斯特联（曼联）
					if (wordList[1].contains("（") && wordList[1].contains("）")) {
						String thisSynonEntity = wordList[1];
						String first = thisSynonEntity.substring(0, thisSynonEntity.indexOf("（"));
						String second = thisSynonEntity.substring(thisSynonEntity.indexOf("（") + 1,
								thisSynonEntity.indexOf("）"));
						entitySyn.put(first.toLowerCase(), wordList[0].toLowerCase()); 
						entitySyn.put(second.toLowerCase(), wordList[0].toLowerCase()); 
					} else {
						entitySyn.put(wordList[1].toLowerCase(), wordList[0].toLowerCase()); 
					}
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return entitySyn;
	}
	
	// create entity table Set
	private static Set<String> createEntityPMTable() {
		Set<String> entitySet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/entityPM.txt";
		System.out.println("path is " + fileName);
		
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
					// all entity in table are in low case
					entitySet.add(word.trim().toLowerCase());
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return entitySet;
	}

	// create entity table Set
	private static Set<String> createEntityTable() {
		Set<String> entitySet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/entity.txt";
		System.out.println("path is " + fileName);

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
					// all entity in table are in low case
					entitySet.add(word.trim().toLowerCase());
				}
				dis.close();

				// File fileDictoray = new File(filePath);
				// File[] allFile = fileDictoray.listFiles();
				// for (File f : allFile) {
				// FileInputStream fis = new FileInputStream(f);
				// InputStreamReader read = new InputStreamReader(fis);
				// BufferedReader dis = new BufferedReader(read);
				// String word = "";
				// while ((word = dis.readLine()) != null) {
				// if (!word.trim().isEmpty())
				// entitySet.add(word.trim());
				// }
				// dis.close();
				// }
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

	// is the string exists in Entity or Entity_Ref
	public static boolean isEntity(String str) {
		if (!getEntityInDictinoary(str).isEmpty() && !getEntitySynonymNormal(str).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	// check whether the work in entityPM.txt
	public static boolean isEntityPM(String str){
		if(!Tool.isStrEmptyOrNull(str) && entityPMTable.contains(str)){
			return true;
		} else {
			return false;
		}
	}
	
	private static String getEntityInDictinoary(String str) {
		if (!Tool.isStrEmptyOrNull(str) && entityTable.contains(str)) {
			return str;
		} else {
			return "";
		}
	}

	// check the entity synonym
	// input-output: 甲肝-甲型病毒性肝炎
	// input-output: 姚明-“”
	public static String getEntitySynonymNormal(String str) {
		if (!Tool.isStrEmptyOrNull(str) && entitySynonymTable.keySet().contains(str))
			return entitySynonymTable.get(str);
		else
			return "";
	}

	// check the entity synonym
	// input-output: 甲型病毒性肝炎-甲肝
	// input-output: 姚明-“”，甲肝-""
	public static String getEntitySynonymReverse(String str) {
		if (!Tool.isStrEmptyOrNull(str) && entitySynonymTable.values().contains(str)) {
			for (String s : entitySynonymTable.keySet()) {
				if (entitySynonymTable.get(s).equals(str))
					return s;
			}
			System.err.println("error in getEntitySynonymReverse");
			return "";
		} else
			return "";
	}

	// check the entity has a synonym
	// input-output: 甲型病毒性肝炎-true
	public static boolean hasEntitySynonym(String str) {
		if (!Tool.isStrEmptyOrNull(str) && entitySynonymTable.values().contains(str))
			return true;
		else
			return false;
	}

	// check whether the word is a synonym of an entity
	// input-output: 甲肝-true
	public static boolean isEntitySynonym(String str) {
		if (!Tool.isStrEmptyOrNull(str) && entitySynonymTable.keySet().contains(str))
			return true;
		else
			return false;
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
							setElementSyn.add(words[j].toLowerCase());
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
		System.err.println("init of synonymtable");
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
							syn.put(words[j].toLowerCase(), ss);
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
	
	// if str in synonym dictionary or not
	public static boolean isInSynonymDict(String str){
		if (!str.isEmpty() && synonymTable.containsKey(str)) {
			return true;
		} else {
			return false;
		}
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

	// remove the elements which are contained in other elements
	// input: [面对面，名人面对面] output: [名人面对面]
	private static List<String> removeContainedElements(TreeSet<String> tSet) {
		TreeSet<String> tempSet = new TreeSet<String>(new StringLengthComparator());
		List<String> rsSet = new ArrayList<>();

		Iterator<String> it = tSet.iterator();
		while (it.hasNext()) {
			String element = it.next();
			it.remove();
			boolean isContained = false;
			for (String s : tSet) {
				if (s.contains(element)) {
					isContained = true;
					break;
				}
			}
			if (isContained == false) {
				tempSet.add(element);
			}
		}

		String[] tempArr = tempSet.toArray(new String[0]);
		// System.out.println("tempArr=" + tempArr);
		for (int i = tempArr.length - 1; i >= 0; i--) {
			rsSet.add(tempArr[i]);
		}

		// System.out.println("rsSet=" + rsSet);
		return rsSet;
	}

	// return the set of entity which is contained in the input sentence
	// TBD: improve the performance after 4/15
	// input: 姚明和叶莉的女儿是谁？
	// output: [姚明，叶莉]
	public static List<String> getEntitySimpleMatch(String sentence) {
		sentence = sentence.toLowerCase();
		TreeSet<String> entityTreeSet = new TreeSet<String>(new StringLengthComparator());
		List<String> entitySet = new ArrayList<>();
		for (String s : entityTable) {
			if (sentence.contains(s.toLowerCase())) {
				entityTreeSet.add(s);
			}
		}
		for (String s : entitySynonymTable.keySet()) {
			if (sentence.contains(s.toLowerCase())) {
				entityTreeSet.add(entitySynonymTable.get(s));
			}
		}

		entitySet = removeContainedElements(entityTreeSet);

		// Iterator<String> it = entityTreeSet.iterator();
		// while (it.hasNext()) {
		// entitySet.add(it.next().toString());
		// }

		System.out.println("the macthed entities are: " + entitySet.toString());
		return entitySet;
	}

	// return the set of entity which is contained in the input sentence by NLP
	// Method
	// input: 姚明和叶莉的女儿是谁？
	// output: [姚明，叶莉]
	public static List<String> getEntityByNLP(List<Term> segPos) {
		List<String> entitySet = new ArrayList<>();
		TreeSet<String> entityTreeSet = new TreeSet<String>(new StringLengthComparator());

		for (int i = 0; i < segPos.size(); i++) {
			String segWord = segPos.get(i).word;
			if (!NLPProcess.getEntityInDictinoary(segWord).isEmpty()) {
				entityTreeSet.add(segWord);
			} else if (!NLPProcess.getEntitySynonymNormal(segWord).isEmpty()) {
				entityTreeSet.add(entitySynonymTable.get(segWord));
			}
		}

		// Iterator<String> it = entityTreeSet.iterator();
		// while (it.hasNext()) {
		// entitySet.add(it.next().toString());
		// }

		entitySet = removeContainedElements(entityTreeSet);
		System.out.println("the result entities of NLP are: " + entitySet.toString());
		return entitySet;
	}

	// remove the stopword in a string.
	// input: "身高是多少"
	// output: "身高"
	public static String removeStopWord(String str) {
		// String rs = "";
		StringBuffer buffer = new StringBuffer();
		// Segmentation Process
		// NLPResult tnNode = NLPSevice.ProcessSentence(str,
		// NLPFlag.SegPos.getValue());
		List<Term> segPos = getSegWord(str);
		System.out.println("original string is " + str);
		// List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			String s = segPos.get(i).word;
			// System.out.print(s + ", ");
			if (!NLPProcess.isStopWord(s))
				buffer.append(s);
		}
		// System.out.println("");

		return buffer.toString();
	}

	// remove the stopword in a string.
	// input: "姚明是谁？"
	// output: "姚明是谁"
	public static String removePunctuateMark(String str) {
		if (Tool.isStrEmptyOrNull(str)) {
			return str;
		}
		if (str.endsWith("？") || str.endsWith("?") || str.endsWith("。") || str.endsWith(".")) {
			return str.substring(0, str.length() - 1);
		}
		return str;
	}

	public static void main(String[] args) {
		String str = "姚明是谁。";
		NLPProcess sp = new NLPProcess();

		System.out.println(sp.removePunctuateMark(str));

		String rs = "";
		NLPResult tnNode = NLPSevice.ProcessSentence(str, NLPFlag.SegPos.getValue());
		List<Term> segPos = tnNode.getWordPos();
		for (int i = 0; i < segPos.size(); i++) {
			String s = segPos.get(i).word;
			rs += s;
		}
		System.out.println(rs);

		// sp.synonymProcess("");

		List<String> es = getEntitySimpleMatch(str);
		for (String s : es) {
			System.out.println(s.length());
		}

		System.out.print("size of entity table is " + entityTable.size());

//		String fileEntity = Common.UserDir + "/knowledgedata/entity.txt";
//
//		try {
//			File writename = new File(fileEntity);
//			writename.createNewFile(); // 创建新文件
//			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
//			for (String s : entityTable) {
//				// System.out.println(s.length());
//				out.write(s + "\r\n"); // \r\n即为换行
//			}
//			out.flush(); // 把缓存区内容压入文件
//			out.close(); // 最后记得关闭文件
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		// System.out.println("syn is " + matchSynonymPropertyInDB("姚明", "女人"));

		// System.out.println("syn is " + sp.getSynonymWord("伯"));

	}
}
