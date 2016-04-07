package com.emotibot.util;

import java.util.Comparator;

public class StringLengthComparator implements Comparator{

	// sort the string by the length in decrease order
	@Override
    public int compare(Object lhs, Object rhs) {
        String s1 = (String) rhs;
        String s2 = (String) lhs;
        int num = new Integer(s1.length()).compareTo(new Integer(s2.length()));
        
        if (num == 0) {
            return s1.compareTo(s2);
        }
        
        return num;
    }
    
}