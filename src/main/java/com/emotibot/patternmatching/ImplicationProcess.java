package com.emotibot.patternmatching;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.emotibot.config.ConfigKeyName;
import com.emotibot.util.Tool;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */

public class ImplicationProcess {
	private static Map<String, String> implicationWordTable = createImplicationWordTable();
	private static final Properties properties = getConfigProperty();

	public static Properties getConfigProperty() {
		Properties prop = new Properties();
		System.out.println(ConfigKeyName.ImplicationConfigName);
		try {
			prop.load(new FileInputStream(ConfigKeyName.ImplicationConfigName));
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw, true));
			String str = sw.toString();
			System.out.println(str);
		}

		return prop;
	}

	public static String getProperty(String prop) {
		String configProp = implicationWordTable.get(prop);
		if (properties != null && properties.containsKey(configProp))
			return properties.getProperty(configProp);
		return "";
	}

	// create Implication Word Table
	private static Map<String, String> createImplicationWordTable() {
		Map<String, String> implicationWordSet = new HashMap<>();
		implicationWordSet.put("年龄", "age"); // TBD: hard code for 4/15
		return implicationWordSet;
	}

	public static boolean isImplicationWord(String str) {
		boolean isSW = false;
		if (!str.isEmpty() && implicationWordTable.keySet().contains(str))
			isSW = true;
		return isSW;
	}
	
	public static PatternMatchingResultBean checkImplicationWord(String str) {
		PatternMatchingResultBean bean = new PatternMatchingResultBean();
		for(String s : implicationWordTable.keySet()){
			if(str.contains(s)){
				bean.setAnswer(s);
				bean.setScore(100);
			}
				
		}
		return bean;
	}
	
	public String getAgeByImplication(String sentence, String entity) {
		Calendar ca = Calendar.getInstance();
		int currentYear = ca.get(Calendar.YEAR);
		String birthInfo = DBProcess.getPropertyValue(entity, "出生日期");
		if(Tool.isStrEmptyOrNull(birthInfo)){
			System.err.println("there is no birth info in entity:"+entity);
			return "";
		}
		
		birthInfo = birthInfo.substring(0, birthInfo.indexOf("年"));
		int birthYear = Integer.parseInt(birthInfo);
		int age = currentYear - birthYear;
		System.out.println("currentYear="+currentYear+", birthYear="+birthYear+", age="+age);
		
		return Integer.toString(age);
	}

	public static String getImplicationAnswer(String sentence, String entity, String prop) {
		if (Tool.isStrEmptyOrNull(entity) || Tool.isStrEmptyOrNull(prop)) {
			System.err.println("entity=" + entity + ", prop=" + prop);
			return "";
		}
		
		String rs = "";

		Class<?> demo = null;
		try {
			demo = Class.forName("com.emotibot.patternmatching.ImplicationProcess");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String methodName = getProperty(prop);
			System.out.println("prop="+prop+", methodName="+methodName+", entity="+entity);
			Method method = demo.getMethod(methodName, String.class, String.class);
			// int year = (int) method.invoke(demo.newInstance());
			rs = (String) method.invoke(demo.newInstance(), sentence, entity);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	public static void main(String[] args) {
		ImplicationProcess ip = new ImplicationProcess();

	}
}
