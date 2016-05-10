package com.emotibot.understanding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.emotibot.patternmatching.DBProcess;
import com.emotibot.patternmatching.NLPProcess;
import com.emotibot.util.IndexInStringComparator;
import com.emotibot.util.StringLengthComparator;
import com.emotibot.util.Tool;

public class NERUtil {
	// remove the elements which are contained in other elements
	// input: [面对面，名人面对面] output: [名人面对面]
	public static List<String> removeContainedElements(TreeSet<String> tSet) {
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

	// sort by the index of the string in the sentence
	protected static List<String> sortByIndexOfSentence(String sentence, List<String> set) {
		// System.out.println("input of the sort is set="+set);
		TreeSet<String> refSet = new TreeSet<String>(new IndexInStringComparator(sentence));
		for (String s : set) {
			refSet.add(s);
		}

		set.clear();
		for (String s : refSet) {
			set.add(s);
		}

		// System.out.println("output of the sort is set="+set);
		return set;
	}

	// get the property set in DB with synonym process
	// return Map<synProp, prop>
	// input: 姚明
	// output: [<老婆,老婆>, <妻,老婆>, ...]
	protected static Map<String, String> getPropertyNameSet(String label, String ent) {
		Map<String, String> rsMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("PMP.getPropertyNameSet: input is empty");
			return rsMap;
		}

		List<String> propList = DBProcess.getPropertyNameSet(label, ent);
		if (propList != null && !propList.isEmpty()) {
			for (String iProp : propList) {
				rsMap.put(iProp, iProp);
				Set<String> setSyn = NLPProcess.getSynonymWordSet(iProp);
				for (String iSyn : setSyn) {
					rsMap.put(iSyn, iProp);
				}
			}
		}
		// System.out.println("all the prop is: " + rsMap);
		return rsMap;
	}
}
