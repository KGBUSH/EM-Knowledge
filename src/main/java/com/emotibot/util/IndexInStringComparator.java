package com.emotibot.util;

import java.util.Comparator;

public class IndexInStringComparator implements Comparator{
	private String sentence = "";
	
	public IndexInStringComparator(String s){
		sentence = s;
	}

	// sort the string by the length in increasing order
	@Override
    public int compare(Object lhs, Object rhs) {
        String sLHS = (String) lhs;
        String sRHS = (String) rhs;
        
        int indexLHS = sentence.indexOf(sLHS);
        int indexRHS = sentence.indexOf(sRHS);
        
        int num = new Integer(indexLHS).compareTo(new Integer(indexRHS));
//        int num = new Integer(sLHS.length()).compareTo(new Integer(sRHS.length()));
        
        if (num == 0) {
            return sLHS.compareTo(sRHS);
        }
        
        return num;
    }
    
}