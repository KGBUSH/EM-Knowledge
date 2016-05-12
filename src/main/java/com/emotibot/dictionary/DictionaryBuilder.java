package com.emotibot.dictionary;

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
import com.emotibot.common.Common;
import com.emotibot.log.LogService;
import com.emotibot.util.CharUtil;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.dictionary.CustomDictionary;

public class DictionaryBuilder {
	private static HashMap<String, Set<String>> synonymTable = createSynonymTable();;
	private static HashMap<String, List<String>> synonymTableRef = createSynonymTableRef();
	private static Set<String> stopWordTable = createStopWordTable();
	private static Set<String> entityPMTable = createEntityPMTable();
	private static Set<String> entityTable = createEntityTable();
	// entitySynonymTable:[甲肝，甲型病毒性肝炎]
	private static Map<String, String> entitySynonymTable = createEntitySynonymTable();
	// entitySynonymReverseTable:[山大，<山西大学，山东大学>]
	private static Map<String, List<String>> entitySynonymReverseTable = createEntitySynonymReverseTable();
	private static Set<String> highFeqWordTable = createHighFeqWordTable();
	private static Set<String> removeableHighFeqWordOtherTable = createRemoveableHighFeqWordOtherTable();
	private static Set<String> removeableHighFeqWordAllTable = createRemoveableHighFeqWordAllTable();
	private static Set<String> domainBalckListTable = createDomainBalckListTable();
	
	public static void DictionaryBuilderInit() {
		addCustomDictionaryInHanlp();
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
	

	public static Set<String> getHighFeqWordTable() {
		return highFeqWordTable;
	}

	// if str in high frequent word dictionary or not
	public static boolean isInHighFreqDict(String str) {
		if (!str.isEmpty() && highFeqWordTable.contains(str)) {
			return true;
		} else {
			return false;
		}
	}

	public static Set<String> getEntityTable() {
		return entityTable;
	}


	public static Map<String, String> getEntitySynonymTable() {
		return entitySynonymTable;
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
						String[] words = CharUtil.trim(line).split(" ");
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
						String[] words = CharUtil.trim(line).split(" ");
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

	// [欧洲，<欧罗巴，欧罗巴洲>]
	private static Map<String, List<String>> createEntitySynonymReverseTable() {
		Map<String, List<String>> rsMap = new HashMap<>();
		for (String s : entitySynonymTable.keySet()) {
			String value = entitySynonymTable.get(s); // 欧洲
			List<String> list = new ArrayList<>();
			if (rsMap.keySet().contains(value)) {
				list = rsMap.get(value);
			}
			list.add(s);
			rsMap.put(value, list);
		}
		return rsMap;
	}

	// create entity table Set
	// [“甲肝”,"甲型病毒性肝炎"]
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
					String[] wordList = CharUtil.trim(line).split("##");
					if (wordList.length < 2) {
						System.err.println("wrong format in entitySynonym.txt");
						continue;
					}
					String dbName = CharUtil.trim(wordList[0]);
					for (int i = 1; i < wordList.length; i++) {
						String synName = CharUtil.trim(wordList[i]);
						// address the case 曼彻斯特联（曼联）
						if (synName.contains("（") && synName.contains("）")) {
							String thisSynonEntity = synName;
							String first = thisSynonEntity.substring(0, thisSynonEntity.indexOf("（"));
							String second = thisSynonEntity.substring(thisSynonEntity.indexOf("（") + 1,
									thisSynonEntity.indexOf("）"));

							entitySyn.put(first.toLowerCase(), dbName.toLowerCase());
							entitySyn.put(second.toLowerCase(), dbName.toLowerCase());
						} else {
							entitySyn.put(synName.toLowerCase(), dbName.toLowerCase());
						}
					}
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		System.out.println("length of entitySyn: " + entitySyn.size());

		return entitySyn;
	}

	// create entity table Set
	private static Set<String> createEntityPMTable() {
		Set<String> entitySet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/entity_ref_PM.txt";
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
				String word = null;
				while ((word = dis.readLine()) != null) {
					// all entity in table are in low case
					entitySet.add(CharUtil.trim(word).toLowerCase());
					// if(word.length() == 1) System.out.println(word);
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		System.out.println("entitySet lengh = " + entitySet.size());
		return entitySet;
	}

	// createHighFeqWordTable
	private static Set<String> createRemoveableHighFeqWordOtherTable() {
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/removeableHighFrequent.txt";
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
				String word = null;
				while ((word = dis.readLine()) != null) {
					// all entity in table are in low case
					wordSet.add(CharUtil.trim(word).toLowerCase());
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		System.out.println("createRemoveableHighFeqWordTable lengh = " + wordSet.size());
		return wordSet;
	}
	
	// createHighFeqWordAllTable
	private static Set<String> createRemoveableHighFeqWordAllTable() {
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/removeableHighFrequentAll.txt";
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
				String word = null;
				while ((word = dis.readLine()) != null) {
					// all entity in table are in low case
					wordSet.add(CharUtil.trim(word).toLowerCase());
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		System.out.println("createRemoveableHighFeqWordAllTable lengh = " + wordSet.size());
		return wordSet;
	}
	
	// createDomainBalckListTable
	private static Set<String> createDomainBalckListTable() {
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/domain/blacklist.txt";
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
				String word = null;
				while ((word = dis.readLine()) != null) {
					// all entity in table are in low case
					wordSet.add(CharUtil.trim(word).toLowerCase());
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		System.out.println("createDomainBalckListTable lengh = " + wordSet.size());
		return wordSet;
	}

	// createHighFeqWordTable
	private static Set<String> createHighFeqWordTable() {
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/highFrequentWordPartOf.txt";
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
				String word = null;
				while ((word = dis.readLine()) != null) {
					// all entity in table are in low case
					wordSet.add(CharUtil.trim(word).toLowerCase());
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		System.out.println("createHighFeqWordTable lengh = " + wordSet.size());
		return wordSet;
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
				String word = null;
				while ((word = dis.readLine()) != null) {
					// all entity in table are in low case
					entitySet.add(CharUtil.trim(word).toLowerCase());
				}
				dis.close();
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
				String word = null;
				while ((word = dis.readLine()) != null) {
					stopWordSet.add(CharUtil.trim(word));
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

	public static Map<String, List<String>> getEntitySynonymReverseTable() {
		return entitySynonymReverseTable;
	}

	public static void setEntitySynonymReverseTable(Map<String, List<String>> entitySynonymReverseTable) {
		DictionaryBuilder.entitySynonymReverseTable = entitySynonymReverseTable;
	}

	public static Set<String> getStopWordTable() {
		return stopWordTable;
	}

	public static void setStopWordTable(Set<String> stopWordTable) {
		DictionaryBuilder.stopWordTable = stopWordTable;
	}

	public static Set<String> getEntityPMTable() {
		return entityPMTable;
	}

	public static void setEntityPMTable(Set<String> entityPMTable) {
		DictionaryBuilder.entityPMTable = entityPMTable;
	}

	public static HashMap<String, Set<String>> getSynonymTable() {
		return synonymTable;
	}

	public static void setSynonymTable(HashMap<String, Set<String>> synonymTable) {
		DictionaryBuilder.synonymTable = synonymTable;
	}

	public static HashMap<String, List<String>> getSynonymTableRef() {
		return synonymTableRef;
	}

	public static void setSynonymTableRef(HashMap<String, List<String>> synonymTableRef) {
		DictionaryBuilder.synonymTableRef = synonymTableRef;
	}

	public static Set<String> getRemoveableHighFeqWordTable() {
		return removeableHighFeqWordOtherTable;
	}

	public static void setRemoveableHighFeqWordTable(Set<String> removeableHighFeqWordTable) {
		DictionaryBuilder.removeableHighFeqWordOtherTable = removeableHighFeqWordTable;
	}

	public static Set<String> getRemoveableHighFeqWordAllTable() {
		return removeableHighFeqWordAllTable;
	}

	public static void setRemoveableHighFeqWordAllTable(Set<String> removeableHighFeqWordAllTable) {
		DictionaryBuilder.removeableHighFeqWordAllTable = removeableHighFeqWordAllTable;
	}

	public static Set<String> getDomainBalckListTable() {
		return domainBalckListTable;
	}

	public static void setDomainBalckListTable(Set<String> domainBalckListTable) {
		DictionaryBuilder.domainBalckListTable = domainBalckListTable;
	}
}
