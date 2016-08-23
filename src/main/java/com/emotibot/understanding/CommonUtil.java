package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.emotibot.common.Common;

public class CommonUtil {

	// get the number elements from souce /\ reference
	protected static List<String> getIntersectionOfTwoLists(List<String> source, List<String> reference, int number) {
		List<String> rsSet = new ArrayList<>();
		for (String s : source) {
			if (reference.contains(s)) {
				rsSet.add(s);
				number--;
			}
			if (number == 0) {
				break;
			}
		}
		return rsSet;
	}


	// to test if two list are equal
	protected static List<String> mergeTwoLists(List<String> lhs, List<String> rhs) {
		List<String> rsList = new ArrayList<>();
		rsList.addAll(lhs);
		for (String s : rhs) {
			if (!lhs.contains(s))
				rsList.add(s);
		}
		return rsList;
	}

	// to match a segword in sentence with some value of a entity.
	protected static String matchPropertyValue(String entity, List<String> segWord) {
		String rs = "";
		Map<String, Object> mapPropValue = DBProcess.getEntityPropValueMap(NLPUtil.getLabelByEntity(entity), entity);
	
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
	

	// to test if two list are equal
	public static boolean isTwoListsEqual(List<String> lhs, List<String> rhs) {
//		System.out.println("llhs="+lhs+", rhs="+rhs);
//		System.out.println("ls="+lhs.size()+", rhs="+rhs.size());
		if (lhs == null && rhs == null){
			return true;
		}
		
		if(lhs.isEmpty() && rhs.isEmpty()){
			return true;
		}

		if (lhs.size() != rhs.size()) {
			System.out.println("lhs="+lhs+", rhs="+rhs);
			return false;
		}
		for (String s : lhs) {
			if (!rhs.contains(s)){
				System.out.println("lhs="+lhs+", rhs="+rhs+", s="+s);
				return false;
			}
		}
		return true;
	}
	
	//parse entity in sentence like “你想知道终极一班的主演之一炎亚纶最近的新闻吗”
	//return 炎亚纶
	public static String parseEntityInSentence(String sentence){
		String entity = "";
		entity = sentence.substring(sentence.indexOf(CommonConstantName.MAINROLE2) + 4, sentence.lastIndexOf(CommonConstantName.IS_MOST));
		System.out.println("the entity in " + sentence + "is " + entity);
		return entity;
	}
	
	//parse entity in sentence like “你想知道王俊凯的代表作品吗”
	//return 炎亚纶
	public static String getIntentPartInSentence(String sentence){
		String intent = "";
		intent = sentence.substring(sentence.indexOf(CommonConstantName.PROPERTYPREFIX) + 4, sentence.lastIndexOf(CommonConstantName.MOODWORD3));
		System.out.println("the intent in " + sentence + "is " + intent);
		return intent;
	}
		
	public static void main(String [] args){
		
		System.out.println(getIntentPartInSentence("你想知道王俊凯的代表作品吗"));
//		JSONArray jsonArray = jsObj.getJSONArray("result");
//		System.out.println(jsObj);
//		System.out.println(json);
//		List<String> lhs = new ArrayList<>();
//		List<String> rhs = new ArrayList<>();
//		System.out.println("rs = "+isTwoListsEqual(lhs, rhs));
	}

}
