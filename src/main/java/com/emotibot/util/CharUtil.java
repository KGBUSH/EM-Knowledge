package com.emotibot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
public class CharUtil {
	 // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    } 
    public static int countChineseCharNum(String strName)
    {
    	int count=0;
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                count++;
            }
        }
        return count;

    }
    public static boolean isEnglisgBigChar(String str){
    	//if(Tool.isStrEmptyOrNull(str)) return false;
        Pattern pattern = Pattern.compile("^[A-Z]+$");
        return pattern.matcher(str).matches();   
     }
    public static String zerolize(String s) {
		if (s.length() < 4) {
			s = "000".substring(0, 4 - s.length()) + s;
		}
		return s;
	}
    
    public static String trim(String s){
    	String str = s.replace(String.valueOf((char) 160), " ").trim();
//    	String test = s.trim();
//    	if(!str.equals(test)){
//    		System.err.println("str="+str+", test="+test);
//    	}
    	return str;
    	
    }
    
    public static String trimAndlower(String s){
    	return s.replace(String.valueOf((char) 160), " ").trim().toLowerCase();
    }
    
    public static boolean isDateFormat(String str){
    	Pattern p=Pattern.compile("\\d+年\\d+月\\d+日"); 
    	Matcher m=p.matcher(str); 
    	return m.matches();
    }
    
    public static boolean isPuncuation(String str){
    	Pattern p=Pattern.compile("[\\pP+~$`^=|<>～`$^+=|<>￥×]"); 
    	Matcher m=p.matcher(str); 
    	return m.matches();
    }
    
    public static void main(String args[])
    {
//    	System.err.println(isEnglisgBigChar("A"));
    	
//    	String testDate = "1727年3月31日";
    	String testDate = "/";
    	
    	System.out.println("is p:"+isPuncuation(testDate));
    	
    	
    }
}
