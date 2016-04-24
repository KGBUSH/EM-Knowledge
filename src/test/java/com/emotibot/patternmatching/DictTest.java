package com.emotibot.patternmatching;

import java.util.Set;

public class DictTest {

	public static void main(String [] args){
		
		Set<String> entitySet = NLPProcess.getEntityTable();
		
		int i = 1;
		for(String s : entitySet){
			if(NLPProcess.isInSynonymDict(s)){
				System.out.println(i+++": "+s);
			}
		}
		
	}
	
}
