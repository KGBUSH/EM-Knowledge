package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.netlib.util.booleanW;

import com.emotibot.dictionary.DictionaryBuilder;
import com.emotibot.log.LogService;
import com.emotibot.util.CharUtil;
import com.emotibot.util.StringLengthComparator;
import com.emotibot.util.StringLengthDecComparator;
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
		if (!getEntityInDictinoary(str).isEmpty() || !getEntitySynonymNormal(str).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	// is the string exists in Entity, without checking entity synonym 
	public static boolean isDBEntity(String str) {
		if (!getEntityInDictinoary(str).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	// check whether the work in entityPM.txt
	public static boolean isFirstLevelEntity(String str) {
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
	// input-output: 九寨沟 - <九寨沟县，九寨沟国家风景区>
	public static List<String> getEntitySynonymNormal(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntitySynonymTable().keySet().contains(str))
			return DictionaryBuilder.getEntitySynonymTable().get(str);
		else
			return new ArrayList<>();
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

//		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntitySynonymTable().values().contains(str)) {
		if(isDBEntityHasSynonym(str)) {
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
	public static boolean isDBEntityHasSynonym(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntitySynonymReverseTable().keySet().contains(str))
			return true;
		else
			return false;
	}

	// check whether the word is a synonym of an entity
	// input-output: 甲肝-true
	public static boolean isASynonymEntity(String str) {
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
	// if there are more than one elements, return them in the order of decreasing length of the word
	public static TreeSet<String> getSynonymnEntityList(String dbEntity) {
		TreeSet<String> entityTreeSet = new TreeSet<String>(new StringLengthDecComparator());
		List<String> list = new ArrayList<>();
		if (!Tool.isStrEmptyOrNull(dbEntity)) {
			list = DictionaryBuilder.getEntitySynonymReverseTable().get(dbEntity);
			if (list == null || list.isEmpty()) {
				System.err.println("NLPProcess.getSynonymnEntityList" + "input=" + dbEntity);
				LogService.printLog("0", "NLPProcess.getSynonymnEntityList", "input=" + dbEntity);
			}
			for(String s : list){
				entityTreeSet.add(s);
			}
			return entityTreeSet;
		}
		return entityTreeSet;
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

	// remove the mood word in the last character in a sentence, if the
	// character is a mood word
	private static String removeMoodWordInLast(String entity, String str) {
		if (Tool.isStrEmptyOrNull(str)) {
			return str;
		}
		
		if(!Tool.isStrEmptyOrNull(entity) && str.endsWith(entity)){
			return str;
		}

		String rtnStr = CharUtil.trimAndlower(str);
		
		if(isEndWithValidMood(str)){
			rtnStr = str.substring(0, str.length() - 1);
		}
		
//		String lastC = str.charAt(str.length() - 1) + "";
//		if (DictionaryBuilder.getMoodWordTable().contains(lastC)) {
//			rtnStr = str.substring(0, str.length() - 1);
//		}
		
//		System.out.println("str="+str+", rtn="+rtnStr);

		return rtnStr;
	}

	public static String removeMoodWordB(String entity, String str) {
		if (Tool.isStrEmptyOrNull(str)) {
			return str;
		}

		String rtnStr = CharUtil.trimAndlower(str);
		
		String revisedStr = removeMoodWordInLast(entity, rtnStr);
		while (!revisedStr.equals(rtnStr)){
			rtnStr = revisedStr;
			revisedStr = removeMoodWordInLast(entity, rtnStr);
		}
		
//		// assumption: there are at most two mood words in a sentence
//		rtnStr = removeMoodWordInLast(entity, rtnStr);
//		rtnStr = removeMoodWordInLast(entity, rtnStr);

		return rtnStr;
	}
	
	public static String removeMoodWord(String entity, String str) {
		
		String rtnA = removeMoodWordA(entity, str);
		String rtnB = removeMoodWordB(entity, str);
		
		if (rtnA.equals(rtnB)) {
			return rtnA;
		} else {
			System.err.println("entity="+entity+", str="+str+", rtA=" + rtnA + ", rtB=" + rtnB);
			return rtnA;
		}
	}
	
	// if a word ends with a mood character
	private static boolean isEndWithValidMood(String word){
		if (Tool.isStrEmptyOrNull(word)) {
			return false;
		}
		
		String lastC = word.charAt(word.length() - 1) + "";
		if (DictionaryBuilder.getMoodWordTable().contains(lastC)) {
			if(isEndWithExceptionMoodWord(word)){
				// case: 姚明是什么， 么 is a mood word, 是什么 is a exceptional mood word
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	// if a word ends with a exceptional mood word
	private static boolean isEndWithExceptionMoodWord(String word){
		if (Tool.isStrEmptyOrNull(word)) {
			return false;
		}
		
		for(String s : DictionaryBuilder.getMoodWordExceptionTable()){
			if(word.endsWith(s)){
				return true;
			}
		}
		
		return false;
	}
	
	// if a sentence begin with “你喜欢”
	public static boolean isStartWithSpecialPrefixWord(String sentence) {
		if (Tool.isStrEmptyOrNull(sentence)) {
			return false;
		}
		for(String s : DictionaryBuilder.getPrefixWordRecogniseTable()){
			if(sentence.startsWith(s)){
				return true;
			}
		}
		
		return false;
	}

	// if a sentence contains "知道"，"认识" then return "知道" or "认识" else return ""
	public static String isContainsInIntroductionPrefixWord(String sentence){
		String result = "";
		if (Tool.isStrEmptyOrNull(sentence)) {
			return result;
		}
		for(String s : DictionaryBuilder.getPrefixWordIntroductionTable()){
			if(sentence.contains(s)){
				return s;
			}
		}
		return result;
	}
	
	// remove the moodword in a sentence
	// input: "姚明在家吗" "姚明在家了吗"
	// output: "姚明在家"
	public static String removeMoodWordA(String entity, String str) {
		String tempStr = str;
//		System.out.println("removeMoodWord() input string is : " + str);
		if (Tool.isStrEmptyOrNull(str)) {
			return "";
		}
		
		if(!Tool.isStrEmptyOrNull(entity) && str.endsWith(entity)){
			return str;
		}
		
		if(str.length() == 1){
			if(DictionaryBuilder.getMoodWordTable().contains(str)){
				return "";
			}else{
				return tempStr;
			}
		}
		
		for(int i = str.length()-1; i >= 0; i-- ){
			String charAtLocal = str.charAt(i)+"";
//			System.out.println(charAtLocal);
			if(!DictionaryBuilder.getMoodWordTable().contains(charAtLocal))
				break;
		
			if(!Tool.isStrEmptyOrNull(entity)&&tempStr.endsWith(entity)){
				break;
			}
			
			if(isEndWithExceptionMoodWord(tempStr)){
				break;
			}
			
//			System.out.println(i);
			tempStr = tempStr.substring(0,  i);
			
		}
		
		
		// 当一个句子的的长度大于等于2时，才有可能有2个语气词的情况
//		if (str.length() >= 2) {
//			String endLastTwo = str.substring(str.length() - 2,
//					str.length() - 1);
//			String endLastOne = str.substring(str.length() - 1, str.length());
//			if (DictionaryBuilder.getMoodWordTable().contains(endLastOne)) {
//				if(DictionaryBuilder.getMoodWordTable().contains(endLastTwo) && !entity.contains(endLastTwo)){
//					tempStr = str.substring(0, str.length() - 2);
//				}else{
//					tempStr = str.substring(0, str.length() - 1);
//				}
//			}else{
//				tempStr = str;
//			}
//		} else {
//			String endLastOne = str.substring(str.length() - 1, str.length());
//			if (DictionaryBuilder.getMoodWordTable().contains(endLastOne))
//				tempStr = str.substring(0, str.length() - 1);
//			else
//				tempStr = str;
//		}
//		System.out.println("removeMoodWord() output string is : " + tempStr);
		return tempStr;
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
	public static boolean isInReservedHighFeqWordDict(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getReservedHighFeqWordTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}
	
	// if str in remove All dictionary or not
	public static boolean isInRemoveableMauallyCollectedDict(String str) {
		if (!str.isEmpty() && DictionaryBuilder.getRemoveableMauallyCollectedWordTable().contains(str)) {
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
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getHighFeqWordTable().contains(str)) {
			return true;
		} else {
			return false;
		}
	}
	
	// if str in daily used dictionary or not
	public static boolean isInDailyUsedWordDict(String str) {
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getDailyUsedWordTable().contains(str)) {
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

	// if str is in IntroductionDomainTable or not
	public static boolean isIntroductionDomainTable(String str){
		if(!str.isEmpty() && DictionaryBuilder.getIntroductionDomainTable().contains(str)){
			return true;
		}else{
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
					System.err.println("@@@@@@@@@@ conflict in Synonym Table");
				synWord.add(DictionaryBuilder.getSynonymTableRef().get(s).get(0));
			}
		}

		// System.out.println("pattern matching: NLPProcess.getSynnoymWordSet
		// input = "+str+", output="+synWord);

		return synWord;
	}

	// return the list of label of the input entity via the dictionary generated based on the offline txt file
	public static List<String> getLabelListByEntity(String str){
		List<String> labelList = new ArrayList<String>();
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntityWithLabelTable().containsKey(str)) {
			labelList = DictionaryBuilder.getEntityWithLabelTable().get(str);
		}
		if(labelList.size() == 0){
			System.err.println("NLPUtil中的getLabelListByEntity()对应的entity "+ str+" 的label没有找到！");
		}
		return labelList;
	}

	// return the label of the input entity via the dictionary generated based on the offline txt file
	public static String getLabelByEntity(String str){
		String label = "";
		List<String> labelList = new ArrayList<String>();
		if (!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getEntityWithLabelTable().containsKey(str)) {
			labelList = DictionaryBuilder.getEntityWithLabelTable().get(str);
		}
		if(labelList.size() != 0){
			label = labelList.get(0);
		}
		System.out.println("NLPUtil中的getLabelByEntity()--------->"+label);
		return label;
	}
	
	// return the domain chinese name by label
	public static String getDomainChineseNameByLabel(String str){
		String name = "";
		if(!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getDomainNameMappingTable().containsKey(str)){
			name = DictionaryBuilder.getDomainNameMappingTable().get(str);
		}
		return name;
	}
	
	// return the label by domain chinese name
	public static String getLabelByDomainChineseName(String str){
		String label = "";
		if(!Tool.isStrEmptyOrNull(str)){
			for(Entry<String, String> entry : DictionaryBuilder.getDomainNameMappingTable().entrySet()){
				if(entry.getValue().equals(str)){
					label = entry.getKey();
				}
			}
		}
		return label;
	}
	
	//judge whether a str contains in the domainNameMapping table provided by pm.
	public static boolean isContainsInDomainNameMappingTable(String str){
		if(!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getDomainNameMappingTable().containsKey(str)){
			return true;
		}else{
			return false;
		}
	}
	
	//judge a label is contains in domain such as <figure,movie,tv,or tourism> which is judge whether is needed to be rewrite back to users.
	public static boolean isContainsInDomainNeededToRewrite(String str){
		if(!Tool.isStrEmptyOrNull(str) && DictionaryBuilder.getDomainWithPriorityOfRelationOrPropertyTable().containsKey(str)){
			return true;
		}else {
			return false;
		}
	}
	
	//get relationList or PropertyList 
	public static List<String> getRelationOrPropertyByEntity(String label,String relationOrProperty){
		List<String> list = new ArrayList<String>();
		if(!Tool.isStrEmptyOrNull(label)&&!Tool.isStrEmptyOrNull(relationOrProperty)){
			list = DictionaryBuilder.getDomainWithPriorityOfRelationOrPropertyTable().get(label).get(relationOrProperty);
		}
		return list;
	}
	
	public static void main(String[] args) {
		DictionaryBuilder.DictionaryBuilderInit();
		System.out.println(isContainsInIntroductionPrefixWord("你知道吗"));
		List<String> list = getRelationOrPropertyByEntity("figure","property");
		System.out.println(list);
		String s = "拜拜了";
		String entity = "";
		System.out.println(removeMoodWord(entity,s));
		s = "九寨沟";
		System.out.println(getLabelByEntity("李白"));
		
		System.out.println("test = "+NLPUtil.getSynonymWordSet("老公"));
		System.out.println("test Synonymn = "+NLPUtil.isInSynonymDict("老婆"));
		
		
//		System.out.println(hasEntitySynonym(s));
//		System.out.println(DictionaryBuilder.getEntitySynonymTable().keySet().size());
//		for (String ss : DictionaryBuilder.getEntitySynonymTable().keySet()) {
//			if(ss.startsWith("妈妈")){
//				System.out.println("key="+ss+", value="+DictionaryBuilder.getEntitySynonymTable().get(ss));
//			}
//
//		}

		// System.out.println(isInRemoveableOtherDict(s));
		// System.out.println(isEntityPM(s));

	}

}
