

package com.emotibot.understanding;

import java.util.HashSet;
import java.util.Set;

import com.emotibot.dictionary.DictionaryBuilder;

public class KGAgentTest {

	public static void main(String [] args){
		
		Set<String> set = new HashSet<>();
		set.add("a");
		set.add("a");
		set.add("b");
		
		for(String s : set){
			System.out.println(s);
		}
		
		
		
		
//		DictionaryBuilder.DictionaryBuilderInit();
//		
//		for(String s : DictionaryBuilder.getEntityTable()){
//			if(NLPUtil.isInDailyUsedWordDict(s)){
//				System.out.println(s);
//			}
//		}
		
		
//		StaticTest t1 = new StaticTest("t1");
//		t1.printString();
//		
//		StaticTest t2 = new StaticTest("t2");
//		t2.printString();
//		
//		t1.printString();
	}
}
