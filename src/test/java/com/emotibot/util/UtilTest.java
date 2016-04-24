package com.emotibot.util;

import java.util.HashMap;
import java.util.Map;

public class UtilTest {
	
	public static void main(String [] args){
		String testBlank = "无忧花开  ";
		testBlank = testBlank.replaceAll("[ |　]", " ").trim();
		testBlank = testBlank.replace(String.valueOf((char) 160), " ");
		System.out.println(testBlank+"|");
		System.out.println(testBlank.trim()+"|");

		int i1 = testBlank.charAt(testBlank.length()-2);
		int i2 = testBlank.charAt(testBlank.length()-1);
		System.out.println(i1);
		System.out.println(i2);
		
		
		if (testBlank.endsWith("　")){
			System.out.println(1);
		}
		
		if (testBlank.endsWith(" ")){
			System.out.println(2);
		}
		
		
		

		
		
		Map<String, Integer> testMap = new HashMap<>();
		testMap.put("1", 1);
		System.out.println("get 1 = "+testMap.get("1"));
		System.out.println("get 1 = "+testMap.get("2"));
		
	}

}
