package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.emotibot.common.Common;
import com.emotibot.log.LogService;
import com.emotibot.patternmatching.DBProcess;
import com.emotibot.patternmatching.NLPProcess;
import com.emotibot.solr.SolrUtil;
import com.emotibot.solr.Solr_Query;
import com.emotibot.util.StringLengthComparator;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;

public class EntityRecognizer {

	// identify the entities in a sentence by SimpleMatching, NLP, Solr
	public static List<String> getEntity(String sentence) {
		System.out.println("PMP.getEntity: sentence=" + sentence);
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getEntity: input is empty");
			return null;
		}

		System.out.println("segPos=" + KGAgent.segPos);
		System.out.println("segWordWithoutStopWord=" + KGAgent.segWordWithoutStopWord);

		List<String> rsEntity = new ArrayList<>();
		List<String> simpleMatchEntity = getEntitySimpleMatch(sentence);
		List<String> nlpEntity = getEntityByNLP(KGAgent.segPos);
		System.out.println("\t simpleMatchingEntity=" + simpleMatchEntity + "\n\t nlpEntity=" + nlpEntity);

		if (CommonUtil.isTwoListsEqual(simpleMatchEntity, nlpEntity)) {
			List<String> solrEntity = getEntityBySolr(sentence, nlpEntity, KGAgent.segWordWithoutStopWord);
			System.out.println("\t solrEntity with entity input=" + solrEntity);

			if (simpleMatchEntity.isEmpty()) {
				if (solrEntity.isEmpty())
					return rsEntity;

				String strEntity = solrEntity.get(0);
				if (!matchPropertyValue(strEntity, KGAgent.segWordWithoutStopWord).isEmpty()) {
					rsEntity.add(strEntity);
				}

				System.out.println("case: 0: rsEntity=" + rsEntity);
				return rsEntity;
			}

			if (simpleMatchEntity.size() == 1) {
				rsEntity = simpleMatchEntity;
				System.out.println("case: 1: rsEntity=" + rsEntity);
				return rsEntity;
			}

			if (QuestionClassifier.isRelationshipQuestion(sentence)) {
				// case: 叶莉和姚明的女儿是谁？
				rsEntity.add(simpleMatchEntity.get(0));
				rsEntity.add(simpleMatchEntity.get(1));
				System.out.println("case: 2: rsEntity=" + rsEntity);
				return rsEntity;
			} else {
				// rsEntity.add(simpleMatchEntity.get(0));
				// change simple match set as source for fixing case:
				// "百变小樱是哪种动漫" and "姚明的老婆的身高是多少"
				List<String> tempList = CommonUtil.getIntersectionOfTwoLists(simpleMatchEntity, solrEntity,
						simpleMatchEntity.size());

				for (String s : tempList) {
					if (NLPProcess.isEntityPM(s)) {
						rsEntity.add(s);
						break;
					}
				}
				if (rsEntity.isEmpty()) {
					// bug fixing for the case of "黄金矿工哪年发行"
					rsEntity.add(simpleMatchEntity.get(0));
					// rsEntity.add(getSimilarEntityFromTwoLists(solrEntity,
					// simpleMatchEntity));
					System.err.println("case check in case 2.5=" + rsEntity);
					// Debug.printDebug(uniqueID, 2, "knowledge", "case check in
					// case 2.5=" + rsEntity);
				}
				System.out.println("case: 2.5: rsEntity=" + rsEntity);
				return rsEntity;
			}
		} else {
			// simeple Matching Entity != nlpEntity
			if (!nlpEntity.isEmpty() && simpleMatchEntity.isEmpty()) {
				System.err.println("simple Matching is empty but nlp is not");
				return nlpEntity;
			}

			if (nlpEntity.isEmpty()) {
				List<String> solrEntity = getEntityBySolr(sentence, null, KGAgent.segWordWithoutStopWord);
				System.out.println("\t solrEntity without entity input=" + solrEntity);

				if (simpleMatchEntity.size() == 1) {
					if (solrEntity.isEmpty()) {
						rsEntity = simpleMatchEntity;
						System.out.println("case: 2.7: rsEntity=" + rsEntity);
						return rsEntity;
					} else if (PropertyRecognizer.hasPropertyInSentence(sentence, "", simpleMatchEntity.get(0))) {
						// case: 猫猫是什么科的？
						rsEntity = simpleMatchEntity;
						System.out.println("case: 3: rsEntity=" + rsEntity);
						return rsEntity;
					} else {
						System.out.println("case 4::::: solrEntity=" + solrEntity);
						if (solrEntity.contains(simpleMatchEntity.get(0))) {
							// 私人订制票房有多少
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 4.1: rsEntity=" + rsEntity);
						} else {
							// case: 熊猫明是谁？
							rsEntity.add(solrEntity.get(0));
							System.out.println("case: 4.2: rsEntity=" + rsEntity);
						}
						return rsEntity;
					}
				} else {
					// size of simple matching is larger than 1
					if (solrEntity.size() < 2) {
						rsEntity = simpleMatchEntity;
						System.out.println("case: 4.7: rsEntity=" + rsEntity);
						return rsEntity;
					} else if (QuestionClassifier.isRelationshipQuestion(sentence)) {
						rsEntity.add(solrEntity.get(0));
						rsEntity.add(solrEntity.get(1));
						System.out.println("case: 5: rsEntity=" + rsEntity);
						return rsEntity;
					} else {
						if (solrEntity.contains(simpleMatchEntity.get(0))) {
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 6.1: rsEntity=" + rsEntity);
						} else {
							rsEntity.add(solrEntity.get(0));
							System.out.println("case: 6.2: rsEntity=" + rsEntity);
						}
						return rsEntity;
					}
				}
			} else {
				// nlp is not empty, return the intersection among the results
				// by three methods
				List<String> mergeEntity = CommonUtil.mergeTwoLists(simpleMatchEntity, nlpEntity);
				List<String> solrEntity = getEntityBySolr(sentence, mergeEntity, KGAgent.segWordWithoutStopWord);
				System.out.println("\t solrEntity with entity input=" + solrEntity + ",\n mergeEntity=" + mergeEntity);

				if (simpleMatchEntity.size() == 1) {
					if (solrEntity.isEmpty()) {
						rsEntity = simpleMatchEntity;
						System.out.println("case: 6.5: rsEntity=" + rsEntity);
						return rsEntity;
					} else {
						if (solrEntity.contains(simpleMatchEntity.get(0))) {
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 7.1: rsEntity=" + rsEntity);
						} else {
							rsEntity.add(solrEntity.get(0));
							System.out.println("case: 7.2: rsEntity=" + rsEntity);
						}
						return rsEntity;
					}
				} else {
					// size of simple matching is larger than 1
					if (QuestionClassifier.isRelationshipQuestion(sentence)) {
						if (solrEntity.size() < 2) {
							rsEntity.add(simpleMatchEntity.get(0));
							rsEntity.add(simpleMatchEntity.get(1));
							System.out.println("case: 7.5: rsEntity=" + rsEntity);
							return rsEntity;
						} else {
							rsEntity = CommonUtil.getIntersectionOfTwoLists(solrEntity, mergeEntity, 2);
							System.out.println("case: 8: rsEntity=" + rsEntity);
							return rsEntity;
						}
					} else {
						if (solrEntity.isEmpty()) {
							rsEntity.add(simpleMatchEntity.get(0));
							System.out.println("case: 8.5: rsEntity=" + rsEntity);
							return rsEntity;
						} else {
							rsEntity = CommonUtil.getIntersectionOfTwoLists(solrEntity, mergeEntity, 1);
							if (rsEntity.isEmpty()) {
								System.out.println("rsEntity is empty");
								rsEntity.add(simpleMatchEntity.get(0));
							}

							System.out.println("case: 9: rsEntity=" + rsEntity);
							return rsEntity;
						}
					}
				}
			}
		}
	}

	// return the set of entity which is contained in the input sentence by NLP
	// Method
	// input: 姚明和叶莉的女儿是谁？
	// output: [姚明，叶莉]
	public static List<String> getEntityByNLP(List<Term> segPos) {
		List<String> entitySet = new ArrayList<>();
		TreeSet<String> entityTreeSet = new TreeSet<String>(new StringLengthComparator());
		Map<String, String> refMap = new HashMap<>();

		for (int i = 0; i < segPos.size(); i++) {
			String segWord = segPos.get(i).word;
			if (!NLPProcess.getEntityInDictinoary(segWord).isEmpty()) {
				entityTreeSet.add(segWord);
			} else if (!NLPProcess.getEntitySynonymNormal(segWord).isEmpty()) {
				// entityTreeSet.add(entitySynonymTable.get(segWord));
				System.out.println(
						"syn in NLP: word=" + segWord + ", syn=" + NLPProcess.getEntitySynonymTable().get(segWord));
				entityTreeSet.add(segWord);
				refMap.put(segWord, NLPProcess.getEntitySynonymTable().get(segWord));
			}
		}

		System.out.println("NLP entities before removal: " + entityTreeSet.toString());
		entitySet = NERUtil.removeContainedElements(entityTreeSet);
		// remove the high frequent entities
		entitySet = NLPProcess.removeRemoveableEntity(entitySet);
		System.out.println("NLP entities after removal: " + entitySet.toString());

		List<String> rsSet = new ArrayList<>();
		for (String s : entitySet) {
			if (refMap.keySet().contains(s)) {
				rsSet.add(refMap.get(s));
			} else {
				rsSet.add(s);
			}
		}

		System.out.println("the NLP entities are: " + rsSet.toString());
		return rsSet;

		// entitySet = removeContainedElements(entityTreeSet);
		// entitySet = removeRemoveableEntity(entitySet);
		// System.out.println("the result entities of NLP are: " +
		// entitySet.toString());
		// return entitySet;

	}

	// return the set of entity which is contained in the input sentence
	// TBD: improve the performance after 4/15
	// input: 姚明和叶莉的女儿是谁？
	// output: [姚明，叶莉]
	public static List<String> getEntitySimpleMatch(String sentence) {
		sentence = sentence.toLowerCase();
		TreeSet<String> entityTreeSet = new TreeSet<String>(new StringLengthComparator());
		List<String> entitySet = new ArrayList<>();
		for (String s : NLPProcess.getEntityTable()) {
			if (sentence.contains(s.toLowerCase())) {
				entityTreeSet.add(s);
			}
		}

		Map<String, String> refMap = new HashMap<>();
		// entitySynonymTable：【甲肝，甲型病毒性肝炎】
		for (String s : NLPProcess.getEntitySynonymTable().keySet()) {
			if (!entityTreeSet.contains(s) && sentence.contains(s.toLowerCase())) {
				entityTreeSet.add(s);
				refMap.put(s, NLPProcess.getEntitySynonymTable().get(s));
			}
		}

		System.out.println("simple matching entities before removal: " + entityTreeSet.toString());
		entitySet = NERUtil.removeContainedElements(entityTreeSet);

		// remove the high frequent entities
		entitySet = NLPProcess.removeRemoveableEntity(entitySet);
		System.out.println("simple matching entities after removal: " + entitySet.toString());
		entitySet = NERUtil.sortByIndexOfSentence(sentence, entitySet);

		List<String> rsSet = new ArrayList<>();
		for (String s : entitySet) {
			if (refMap.keySet().contains(s)) {
				rsSet.add(refMap.get(s));
			} else {
				rsSet.add(s);
			}
		}

		System.out.println("the macthed entities are: " + rsSet.toString());
		return rsSet;
	}
	

	// return the entity by Solr method
	// input: the sentence from user, "姚明身高多少"
	// output: the entity identified by Solr, "姚明"
	protected static List<String> getEntityBySolr(String sentence, List<String> entitySet, List<String> segWord) {
		System.out.println("getEntityBySolr: segWord=" + segWord);
		List<String> rsEntitySet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(sentence)) {
			System.err.println("PMP.getEntityBySolr: input is empty");
			return rsEntitySet;
		}

		SolrUtil solr = new SolrUtil();
		Solr_Query obj = new Solr_Query();

		if (entitySet != null && !entitySet.isEmpty()) {
			obj.setFindEntity(true);
			obj.setEntity(entitySet);
		}

		for (String s : segWord) {
			obj.addWord(s);

			// if (!NLPProcess.isInHighFreqDict(s)) {
			// obj.addWord(s);
			// }

		}

		rsEntitySet = solr.Search(obj);
		System.out.println("getEntityBySolr return: " + rsEntitySet);
		return rsEntitySet;
	}

	// to match a segword in sentence with some value of a entity.
	protected static String matchPropertyValue(String entity, List<String> segWord) {
		String rs = "";
		Map<String, Object> mapPropValue = DBProcess.getEntityPropValueMap("", entity);

		// if a value contain a segword, then return the key which refer to the
		// value
		for (Object value : mapPropValue.values()) {
			for (String s : segWord) {
				if (value.toString().contains(s)) {
					for (String key : mapPropValue.keySet()) {
						if (value.equals(mapPropValue.get(key))) {
							if (key.equals(Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME)) {
								continue; // if the word comes from
											// introduction, remove
							}
							System.out.println(
									"\t matchPropertyValue: key=" + key + ", value=" + value + ", segword=" + s);
							return s + "----####" + key;
						}
					}
				}
			}
		}

		return rs;
	}
	


	// if the sentnece contain a alias, change to wiki entity name
	// input: “甲肝是什么” output: “甲型病毒性肝炎是什么”
	protected static String changeEntitySynonym(List<String> entitySet, String sentence) {
		System.out.println("changeEntitySynonym: entitySet=" + entitySet + ",sentence=" + sentence);
		for (String entity : entitySet) {
			if (NLPProcess.hasEntitySynonym(entity)) {
				List<String> list = NLPProcess.getSynonymnEntityList(entity);
				System.out.println("entity=" + entity + ", synonymList=" + list);
				String oldEntity = "";
				for (String s : list) {
					if (sentence.contains(s)) {
						oldEntity = s;
						break;
					}
				}
				if (oldEntity.isEmpty() || !sentence.contains(entity)) {
					LogService.printLog(KGAgent.uniqueID, "PatternMatching.changeEntitySynonym", "entity=" + entity);
					System.err.println("PatternMatching.changeEntitySynonym: entity=" + entity);
				}
				// String oldEntity =
				// NLPProcess.getEntitySynonymReverse(entity).toLowerCase();
				if (!oldEntity.isEmpty()) {
					sentence = sentence.toLowerCase().replace(oldEntity, entity);
				}
				System.out.println("changeEntitySynonym change : s = " + entity + ", oldEntity=" + oldEntity
						+ "; sentence=" + sentence);
			}
		}
		return sentence;
	}


	// remove stopword and other abnormal word in entity
	protected static void removeAbnormalEntity(List<String> entitySet) {
		Iterator<String> it = entitySet.iterator();
		while (it.hasNext()) {
			String tempEntity = it.next();

			if (NLPProcess.isInRemoveableDict(tempEntity)) {
				System.out.println("remove entity:" + tempEntity);
				it.remove();
			}
		}
	}
	
	
}
