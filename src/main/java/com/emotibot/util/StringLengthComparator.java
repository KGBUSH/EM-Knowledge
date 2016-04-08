package com.emotibot.util;

import java.util.Comparator;

public class StringLengthComparator implements Comparator{

	// sort the string by the length in increasing order
	@Override
    public int compare(Object lhs, Object rhs) {
        String s1 = (String) lhs;
        String s2 = (String) rhs;
        int num = new Integer(s1.length()).compareTo(new Integer(s2.length()));
        
        if (num == 0) {
            return s1.compareTo(s2);
        }
        
        return num;
    }
    
}