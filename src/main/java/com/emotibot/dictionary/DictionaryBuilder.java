package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import com.emotibot.log.LogService;
import com.emotibot.understanding.CommonUtil;
import com.emotibot.understanding.NLPUtil;
import com.emotibot.util.CharUtil;
import com.emotibot.util.StringLengthComparator;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.dictionary.CustomDictionary;

public class DictionaryBuilder {
	// private static HashMap<String, Set<String>> synonymTable =
	// createSynonymTable();
	// private static HashMap<String, List<String>> synonymTableRef =
	// createSynonymTableRef();
	// private static Set<String> stopWordTable = createStopWordTable();
	// // entityPMTable: the first level entity list
	// private static Set<String> entityPMTable = createEntityPMTable();
	// private static Set<String> entityTable = createEntityTable();
	// // entitySynonymTable:[甲肝，甲型病毒性肝炎]
	// private static Map<String, String> entitySynonymTable =
	// createEntitySynonymTable();
	// // entitySynonymReverseTable:[山大，<山西大学，山东大学>]
	// private static Map<String, List<String>> entitySynonymReverseTable =
	// createEntitySynonymReverseTable();
	// // highFeqWordTable: the first 10000 high frequent word from NLP
	// dictionary
	// private static Set<String> highFeqWordTable = createHighFeqWordTable();
	// // removeableHighFeqWordOtherTable: the removeable entity in secondary
	// level
	// private static Set<String> removeableHighFeqWordOtherTable =
	// createRemoveableHighFeqWordOtherTable();
	// // removeableHighFeqWordAllTable: all the removeable entity
	// private static Set<String> removeableHighFeqWordAllTable =
	// createRemoveableHighFeqWordAllTable();
	// private static Set<String> domainAllListTable =
	// createDomainAllListTable();
	// private static Set<String> domainBalckListTable =
	// createDomainBalckListTable();
	// private static Set<String> domainWhiteListTable =
	// createDomainWhiteListTable();

	private static HashMap<String, Set<String>> synonymTable;
	private static HashMap<String, List<String>> synonymTableRef;
	private static Set<String> stopWordTable;
	// entityPMTable: the first level entity list
	private static Set<String> entityPMFirstLevelTable;
	private static Set<String> entityTable;
	// entitySynonymTable:[甲肝，甲型病毒性肝炎]
	private static Map<String, List<String>> entitySynonymTable;
	// entitySynonymReverseTable:[山大，<山西大学，山东大学>]
	private static Map<String, List<String>> entitySynonymReverseTable;
	// highFeqWordTable: the first 10000 high frequent word from NLP dictionary
	private static Set<String> highFeqWordTable;
	// dailyUsedWordTable: the word daily used
	private static Set<String> dailyUsedWordTable;
	// removeableHighFeqWordOtherTable: the removeable entity in secondary level
	private static Set<String> removeableHighFeqWordOtherTable;
	// removeableHighFeqWordAllTable: all the removeable entity
	private static Set<String> removeableHighFeqWordAllTable;
	// removeableMauallyCollectedWordTable: all the removeable entity
	private static Set<String> removeableMauallyCollectedWordTable;
	// the high frequent words which are reserved, not be deleted
	private static Set<String> reservedHighFeqWordTable;
	private static Set<String> domainAllListTable;
	private static Set<String> domainBalckListTable;
	private static Set<String> domainWhiteListTable;
	// moodWordTable :[啊，呢，吧]
	private static Set<String> moodWordTable;
	// moodWordExceptionTable :[是什么]
	private static Set<String> moodWordExceptionTable;
	// entityLabelTable:<女医·明妃传,<other,tv>>
	private static Map<String, List<String>> entityWithLabelTable;

	public static void DictionaryBuilderInit() {
		moodWordTable = createMoodWordTable();
		setMoodWordExceptionTable(createMoodWordExceptionTable());
		setReservedHighFeqWordTable(createReservedHighFeqWordTable());
		highFeqWordTable = createHighFeqWordTable();
		synonymTable = createSynonymTable();
		synonymTableRef = createSynonymTableRef();
		stopWordTable = createStopWordTable();
		entityPMFirstLevelTable = createEntityPMTable();
		entityTable = createEntityTable();
		entitySynonymTable = createEntitySynonymTable();
		entitySynonymReverseTable = createEntitySynonymReverseTable();
		setDailyUsedWordTable(createDailyUsedWordTable());
		removeableHighFeqWordOtherTable = createRemoveableHighFeqWordOtherTable();
		removeableHighFeqWordAllTable = createRemoveableHighFeqWordAllTable();
		removeableMauallyCollectedWordTable = createRemoveableMauallyCollectedWordTable();
		domainAllListTable = createDomainAllListTable();
		domainBalckListTable = createDomainBalckListTable();
		domainWhiteListTable = createDomainWhiteListTable();
		entityWithLabelTable = createEntityWithLabelTable();
		addCustomDictionaryInHanlp();

	}

	// add all the words in entitySynonymn into the Hanlp data
	private static void addCustomDictionaryInHanlp() {
		for (String s : entityTable) {
			CustomDictionary.add(s);
		}
		for (String s : entitySynonymTable.keySet()) {
			CustomDictionary.add(s);
		}
		for (List<String> s : entitySynonymTable.values()) {
			Iterator<String> iterator = s.iterator();
			while (iterator.hasNext()) {
				String string = (String) iterator.next();
				CustomDictionary.add(string);
			}
		}
	}

	public static Set<String> getHighFeqWordTable() {
		return highFeqWordTable;
	}

	public static Set<String> getEntityTable() {
		return entityTable;
	}

	public static Map<String, List<String>> getEntitySynonymTable() {
		return entitySynonymTable;
	}

	// create synonym reference hash map table: Map<id, List of Synonym>
	// format: ["海拔",<海拔，标高>]
	private static HashMap<String, List<String>> createSynonymTableRef() {
		System.err.println("init of createSynonymTableRef");
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
							String tmpS = CharUtil.trimAndlower(words[j]);
							if (!Tool.isStrEmptyOrNull(tmpS)) {
								setElementSyn.add(words[j].toLowerCase());
							}
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
	// format: ["海拔",<Dn01A09, Za10052>]
	private static HashMap<String, Set<String>> createSynonymTable() {
		System.err.println("init of synonymtable");
		HashMap<String, Set<String>> synMap = new HashMap<>();
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
							if (synMap.containsKey(words[j])) {
								ss = synMap.get(words[j]);
							} else {
								ss = new HashSet<String>();
							}
							ss.add(id);
							synMap.put(words[j].toLowerCase(), ss);
						}
					}
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return synMap;
	}

	// [欧洲，<欧罗巴，欧罗巴洲>] 合并下面两个方法
	private static Map<String, List<String>> createEntitySynonymReverseTable() {
		Map<String, List<String>> rsMap1 = createEntitySynonymReverseTable_One();
		Map<String, List<String>> rsMap2 = createEntitySynonymReverseTable_Two();

		for (String str1 : rsMap1.keySet()) {
			if (!rsMap2.keySet().contains(str1)) {
				System.out.println("wrong format 1: s1=" + str1);
			} else {
				if (!CommonUtil.isTwoListsEqual(rsMap1.get(str1), rsMap2.get(str1))) {
					System.out.println("wrong format 2 = " + str1);
				}
			}
		}
		return rsMap1;

		// if(rsMap1.equals(rsMap2)){
		// rsMap = rsMap1;
		// }else {
		// System.err.print("createEntitySynonymReverseTable()方法中两个方法生成的数据表不一样");
		// }
		//
	}

	// [欧洲，<欧罗巴，欧罗巴洲>] 从entitySynonymTable 中获取
	// [九寨沟风景区, <九寨沟>]
	private static Map<String, List<String>> createEntitySynonymReverseTable_One() {
		System.err.println("init of createEntitySynonymReverseTable");
		Map<String, List<String>> rsMap = new HashMap<>();
		for (String s : entitySynonymTable.keySet()) {
			List<String> value = entitySynonymTable.get(s); // 欧洲
			Iterator<String> iterator = value.iterator();
			while (iterator.hasNext()) {
				String string = (String) iterator.next();
				// if contained, update the list, otherwise, create a new one
				if (rsMap.keySet().contains(string)) {
					if (!rsMap.get(string).contains(s)) {
						rsMap.get(string).add(s);
					}
				} else {
					List<String> list = new ArrayList<>();
					list.add(s);
					rsMap.put(string, list);
				}
			}
		}
		return rsMap;
	}

	// [欧洲，<欧罗巴，欧罗巴洲>] 直接从文件中读取
	private static Map<String, List<String>> createEntitySynonymReverseTable_Two() {
		System.err.println("init of createEntitySynonymReverseTable");
		String fileName = Common.UserDir + "/knowledgedata/entitySynonym.txt";
		System.out.println("path is " + fileName);
		Map<String, List<String>> entitySyn = new HashMap<>();

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

					if (entitySyn.containsKey(dbName)) {
						for (int i = 1; i < wordList.length; i++) {
							String synName = CharUtil.trimAndlower(wordList[i]);
							if (!entitySyn.get(dbName).contains(synName)) {
								entitySyn.get(dbName).add(synName);
							}
						}
					} else {
						List<String> list = new ArrayList<String>();
						for (int i = 1; i < wordList.length; i++) {
							String synName = CharUtil.trimAndlower(wordList[i]);
							list.add(synName);
						}
						entitySyn.put(dbName, list);
					}
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		System.out.println("length of createEntitySynonymReverseTable_Two: " + entitySyn.size());

		return entitySyn;
	}

	// create entity table Set
	// [“甲肝”,<"甲型病毒性肝炎">]
	private static Map<String, List<String>> createEntitySynonymTable() {
		System.err.println("init of createEntitySynonymTable");
		String fileName = Common.UserDir + "/knowledgedata/entitySynonym.txt";
		System.out.println("path is " + fileName);
		Map<String, List<String>> entitySyn = new HashMap<>();

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
					// if there is no entry for this entity, create one

					for (int i = 1; i < wordList.length; i++) {
						String synName = CharUtil.trimAndlower(wordList[i]);
						buildEntitySynonymMap(entitySyn, synName.toLowerCase(), dbName.toLowerCase());

						// suggested by Yinhao Lu, there are more case which
						// should not be addressed as follow logic
						// // address the case 曼彻斯特联（曼联）
						// if (synName.contains("（") && synName.contains("）")) {
						// String thisSynonEntity = synName;
						// String first = thisSynonEntity.substring(0,
						// thisSynonEntity.indexOf("（"));
						// String second =
						// thisSynonEntity.substring(thisSynonEntity.indexOf("（")
						// + 1,
						// thisSynonEntity.indexOf("）"));
						//
						// // entitySyn.put(first.toLowerCase(),
						// // dbName.toLowerCase());
						// // entitySyn.put(second.toLowerCase(),
						// // dbName.toLowerCase());
						//
						// buildEntitySynonymMap(entitySyn, first.toLowerCase(),
						// dbName.toLowerCase());
						// buildEntitySynonymMap(entitySyn,
						// second.toLowerCase(), dbName.toLowerCase());
						// } else {
						// // entitySyn.put(synName.toLowerCase(),
						// // dbName.toLowerCase());
						// buildEntitySynonymMap(entitySyn,
						// synName.toLowerCase(), dbName.toLowerCase());
						// }
					}
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		int count = 30;
		for(String s : entitySyn.keySet()){
			if(entitySyn.get(s).size()>1){
				count --;
				System.out.println(s +":"+entitySyn.get(s));
			}
			if(count<=0) break;
		}
		count = 30;

		for (String entity : entitySyn.keySet()) {
			if (entitySyn.get(entity).size()>1 && !NLPUtil.isFirstLevelEntity(entitySyn.get(entity).get(0))) {
				if(count -- > 0){
					System.out.println("order chagne for ::: " + entity +":"+entitySyn.get(entity));
				}
				List<String> tempList = new ArrayList<String>();
				Iterator<String> listIterator = entitySyn.get(entity).iterator();
				while (listIterator.hasNext()) {
					String tempEntity = listIterator.next();
					if (!NLPUtil.isFirstLevelEntity(tempEntity)) {
						listIterator.remove();
						tempList.add(tempEntity);
					} else {
						break;
					}
				}
				
				for(String s : tempList){
					entitySyn.get(entity).add(s);
				}
				
				if(count > 0){
					System.out.println("\t after change" +":"+entitySyn.get(entity));
				}
			}
		}

		System.out.println("length of createEntitySynonymTable: " + entitySyn.size());

		return entitySyn;
	}

	// add an element into the set of the map
	private static void buildEntitySynonymMap(Map<String, List<String>> map, String key, String value) {

		if (Tool.isStrEmptyOrNull(key)) {
			System.err.println("buildEntitySynonymMap: wrong format");
			return;
		}
		// System.out.println("key="+key);
		if (key.length() <= 1 && NLPUtil.isInHighFrequentDict(key)) {
			System.out.println("ignored entity synonym is " + key + ", value=" + value);
			return;
		} else {
			// if the corresponding set does not contain value, add it to the
			// set
			if (map.containsKey(key)) {
				if (!map.get(key).contains(value)) {
					map.get(key).add(value);
				}
			} else {
				List<String> list = new ArrayList<String>();
				list.add(value);
				map.put(key, list);
			}

		}
	}

	// create entity table Set
	private static Set<String> createEntityPMTable() {
		System.err.println("init of createEntityPMTable");
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
		System.err.println("init of createRemoveableHighFeqWordOtherTable");
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

	// createReservedHighFeqWordTable
	private static Set<String> createReservedHighFeqWordTable() {
		System.err.println("init of createReservedHighFeqWordTable");
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/reservedHighFeqWord.txt";
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

		System.out.println("createReservedHighFeqWordTable lengh = " + wordSet.size());
		return wordSet;
	}

	// createRemoveableMauallyCollectedWordTable
	private static Set<String> createRemoveableMauallyCollectedWordTable() {
		System.err.println("init of createRemoveableMauallyCollectedWordTable");
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/removeableMannuallyCollected.txt";
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

		System.out.println("createRemoveableMauallyCollectedWordTable lengh = " + wordSet.size());
		return wordSet;
	}

	// createHighFeqWordAllTable
	private static Set<String> createRemoveableHighFeqWordAllTable() {
		System.err.println("init of createRemoveableHighFeqWordAllTable");
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
	private static Set<String> createDomainWhiteListTable() {
		System.err.println("init of createDomainWhiteListTable");
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/domain/whitelist.txt";
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

	// createDomainBalckListTable
	private static Set<String> createDomainBalckListTable() {
		System.err.println("init of createDomainBalckListTable");
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

	// createDomainAllListTable
	private static Set<String> createDomainAllListTable() {
		System.err.println("init of createDomainAllListTable");
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/domain/domainList.txt";
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
					if (word.startsWith("##")) {
						continue;
					}
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
		System.err.println("createHighFeqWordTable init");
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

		System.err.println("createHighFeqWordTable lengh = " + wordSet.size());
		return wordSet;
	}

	// createHighFeqWordTable
	private static Set<String> createDailyUsedWordTable() {
		System.err.println("createHighFeqWordTable init");
		Set<String> wordSet = new HashSet<>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/dailyUsedWord.txt";
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
					word = CharUtil.trim(word).toLowerCase();
					if (NLPUtil.isEntity(word) && !NLPUtil.isInReservedHighFeqWordDict(word)) {
						wordSet.add(word);
					}

					// fix the bad case of "你是谁"
					word = CharUtil.trimAndlower(NLPUtil.removeMoodWord("", word));
					if (NLPUtil.isEntity(word) && !NLPUtil.isInReservedHighFeqWordDict(word)) {
						wordSet.add(word);
					}
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		System.err.println("createDailyUsedWordTable lengh = " + wordSet.size());
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

	// create moodword table Set
	private static Set<String> createMoodWordTable() {
		Set<String> moodWordSet = new HashSet<String>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/moodWords.txt";
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
					moodWordSet.add(CharUtil.trimAndlower(word));
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return moodWordSet;
	}

	// create moodword table Set
	private static Set<String> createMoodWordExceptionTable() {
		Set<String> moodWordExceptionSet = new HashSet<String>();
		String fileName = Common.UserDir + "/knowledgedata/dictionary/moodExceptionWords.txt";
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
					moodWordExceptionSet.add(CharUtil.trimAndlower(word));
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return moodWordExceptionSet;
	}

	// create entitywithlabel table
	private static Map<String, List<String>> createEntityWithLabelTable() {
		Map<String, List<String>> entityLabel = new HashMap<String, List<String>>();
		String fileName = Common.UserDir + "/knowledgedata/entitywithlabel.txt";
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
					String[] tempWord = word.split("###");
					List<String> tempLabel = null;
					if (entityLabel.containsKey(tempWord[0])) {
						tempLabel = entityLabel.get(tempWord[0]);
					} else {
						tempLabel = new ArrayList<String>();
					}
					tempLabel.add(tempWord[1]);
					entityLabel.put(tempWord[0], tempLabel);
				}
				dis.close();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return entityLabel;
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
		return entityPMFirstLevelTable;
	}

	public static void setEntityPMTable(Set<String> entityPMTable) {
		DictionaryBuilder.entityPMFirstLevelTable = entityPMTable;
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

	public static Set<String> getDomainAllListTable() {
		return domainAllListTable;
	}

	public static void setDomainAllListTable(Set<String> domainAllListTable) {
		DictionaryBuilder.domainAllListTable = domainAllListTable;
	}

	public static Set<String> getDomainWhiteListTable() {
		return domainWhiteListTable;
	}

	public static void setDomainWhiteListTable(Set<String> domainWhiteListTable) {
		DictionaryBuilder.domainWhiteListTable = domainWhiteListTable;
	}

	public static Set<String> getDailyUsedWordTable() {
		return dailyUsedWordTable;
	}

	public static void setDailyUsedWordTable(Set<String> dailyUsedWordTable) {
		DictionaryBuilder.dailyUsedWordTable = dailyUsedWordTable;
	}

	public static Set<String> getMoodWordTable() {
		return moodWordTable;
	}

	public static void setMoodWordTable(Set<String> moodWordTable) {
		DictionaryBuilder.moodWordTable = moodWordTable;
	}

	public static Set<String> getMoodWordExceptionTable() {
		return moodWordExceptionTable;
	}

	public static void setMoodWordExceptionTable(Set<String> moodWordExceptionTable) {
		DictionaryBuilder.moodWordExceptionTable = moodWordExceptionTable;
	}

	public static Set<String> getReservedHighFeqWordTable() {
		return reservedHighFeqWordTable;
	}

	public static void setReservedHighFeqWordTable(Set<String> reservedHighFeqWordTable) {
		DictionaryBuilder.reservedHighFeqWordTable = reservedHighFeqWordTable;
	}

	public static Set<String> getRemoveableMauallyCollectedWordTable() {
		return removeableMauallyCollectedWordTable;
	}

	public static Map<String, List<String>> getEntityWithLabelTable() {
		return entityWithLabelTable;
	}

	public static void setEntityWithLabelTable(Map<String, List<String>> entityWithLabelTable) {
		DictionaryBuilder.entityWithLabelTable = entityWithLabelTable;
	}

	public static void setRemoveableMauallyCollectedWordTable(Set<String> removeableMauallyCollectedWordTable) {
		DictionaryBuilder.removeableMauallyCollectedWordTable = removeableMauallyCollectedWordTable;
	}

	public static void main(String[] a) {
		DictionaryBuilder.DictionaryBuilderInit();

		String testStr = "马刺队";

		System.out.println("rs = " + getEntitySynonymTable().get(testStr));
		testStr = "九寨沟";
		System.out.println("rs = " + getEntitySynonymTable().get(testStr));

	}

}
