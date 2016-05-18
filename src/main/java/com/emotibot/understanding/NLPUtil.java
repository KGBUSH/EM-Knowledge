package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.log.LogService;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class NLPUtil {

	public static boolean isStopWord(String str) {
		boolean isSW = false;
		if (!str.isEmpty() && DictionaryBuilder.getStopWordTable().contains(str)) {
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
	public static boolean isEntityPM(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntityPMTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}

	public static String getEntityInDictinoary(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntityTable().contains(str)) {
			return str;
		} else {
			return "";
		}
	}

	// check the entity synonym
	// input-output: 甲肝-甲型病毒性肝炎
	// input-output: 姚明-“”
	public static String getEntitySynonymNormal(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntitySynonymTable().keySet().contains(str))
			return DictionaryBuilder.getEntitySynonymTable().get(str);
		else
			return "";
	}

	// check the entity synonym
	// input-output: 甲型病毒性肝炎-甲肝
	// input-output: 姚明-“”，甲肝-""
	public static String getEntitySynonymReverse(String str) {
		// if (!Tool.isStrEmptyOrNull(str)) {
		// if(!entitySynonymReverseTable.keySet().contains(str)){
		// System.err.println("there is no synEntity:"+str);
		// return "";
		// }
		// return entitySynonymReverseTable.get(str);
		// }
		// return "";

		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntitySynonymTable().values().contains(str)) {
			for (String s : DictionaryBuilder.getEntitySynonymTable().keySet()) {
				if (DictionaryBuilder.getEntitySynonymTable().get(s).equals(str))
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
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntitySynonymTable().values().contains(str))
			return true;
		else
			return false;
	}

	// check whether the word is a synonym of an entity
	// input-output: 甲肝-true
	public static boolean isEntitySynonym(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntitySynonymTable().keySet().contains(str))
			return true;
		else
			return false;
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

	// remove the removeable string in the set
	public static Set<String> removeRemoveableEntity(Set<String> entitySet) {
		Set<String> rsSet = new HashSet<>();
		for (String s : entitySet) {
			if (!isInRemoveableOtherDict(s)) {
				rsSet.add(s);
			}
		}
		return rsSet;
	}

	// remove the removeable string in the set
	public static List<String> removeRemoveableEntity(List<String> entitySet) {
		List<String> rsSet = new ArrayList<>();
		for (String s : entitySet) {
			if (!isInRemoveableOtherDict(s)) {
				rsSet.add(s);
			}
		}
		return rsSet;
	}

	// if str in remove Other dictionary or not
	public static boolean isInRemoveableOtherDict(String str) {
		if (!str.isEmpty() && DictionaryBuilder.getRemoveableHighFeqWordTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}

	// if str in remove All dictionary or not
	public static boolean isInRemoveableAllDict(String str) {
		if (!str.isEmpty() && DictionaryBuilder.getRemoveableHighFeqWordAllTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}

	// if str in remove All dictionary or not
	public static boolean isInHighFrequentDict(String str) {
		if (!str.isEmpty() && DictionaryBuilder.getHighFeqWordTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}

	// if str in domain balcklist or not
	public static boolean isInDomainWhiteListDict(String str) {
		if (!str.isEmpty() && DictionaryBuilder.getDomainWhiteListTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}
	
	// if str in domain balcklist or not
	public static boolean isInDomainBalckListDict(String str) {
		if (!str.isEmpty() && DictionaryBuilder.getDomainBalckListTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}

	// if str in synonym dictionary or not
	public static boolean isInSynonymDict(String str) {
		if (!str.isEmpty() && DictionaryBuilder.getSynonymTable().containsKey(str)) {
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

		if (!str.isEmpty() && DictionaryBuilder.getSynonymTable().containsKey(str)) {
			synSet = DictionaryBuilder.getSynonymTable().get(str);

			for (String s : synSet) {
				if (!DictionaryBuilder.getSynonymTableRef().containsKey(s))
					System.out.println("@@@@@@@@@@ conflict in Synonym Table");
				synWord.add(DictionaryBuilder.getSynonymTableRef().get(s).get(0));
			}
		}

		// System.out.println("pattern matching: NLPProcess.getSynnoymWordSet
		// input = "+str+", output="+synWord);

		return synWord;
	}

	public static void main(String[] args) {
		String s = "妈妈咪呀！";
		System.out.println(hasEntitySynonym(s));
		System.out.println(DictionaryBuilder.getEntitySynonymTable().keySet().size());
		for (String ss : DictionaryBuilder.getEntitySynonymTable().keySet()) {
			if(ss.startsWith("妈妈")){
				System.out.println("key="+ss+", value="+DictionaryBuilder.getEntitySynonymTable().get(ss));
			}

		}

		// System.out.println(isInRemoveableOtherDict(s));
		// System.out.println(isEntityPM(s));

	}

}
