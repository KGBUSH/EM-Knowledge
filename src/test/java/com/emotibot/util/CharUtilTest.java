package com.emotibot.util;


import java.util.HashSet;
import java.util.Set;

import com.emotibot.util.*;
public class CharUtilTest {
	
	public static void main(String args[])
	{
		Set<String> wordSet =  new HashSet<>();
		wordSet.add("test");
		System.out.println(wordSet.size());
		
		
		System.out.println(CharUtil.isChinese("asx"));
		System.out.println(CharUtil.isChinese(" 她默默支持公益事业，曾担任中国扶贫基金会爱心大使（新浪娱乐评）。"));
		System.out.println(CharUtil.isChinese("asx"));
		System.out.println(CharUtil.isChinese("asx"));
		System.out.println(CharUtil.isChinese("asx"));
		System.out.println(CharUtil.isChinese("asx"));

	}

}
