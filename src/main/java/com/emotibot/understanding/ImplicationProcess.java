package com.emotibot.understanding;

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

import com.emotibot.common.Common;
import com.emotibot.config.ConfigKeyName;
import com.emotibot.util.CharUtil;
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

	public static String getMethodName(String prop) {
		System.out.println("Implication.getMethodName: prop="+prop);
		String configProp = implicationWordTable.get(prop);
		if (properties != null && properties.containsKey(configProp))
			return properties.getProperty(configProp);
		return "";
	}

	// create Implication Word Table
	private static Map<String, String> createImplicationWordTable() {
		Map<String, String> implicationWordSet = new HashMap<>();
		implicationWordSet.put(CommonConstantName.AGE_CN, CommonConstantName.AGE_EN); // TBD: hard code for 4/15
		implicationWordSet.put(CommonConstantName.COMPUTE_YEAR_EN, CommonConstantName.COMPUTE_YEAR_EN); // TBD: hard code for 4/15
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
				bean.setScore(500);
			}
				
		}
		return bean;
	}
	
	// compute the number of year by the given date
	public static String computeYears4Implication(String answer, String entity, String key) {
		System.out.println("computeYears4Implication: answer = "+answer);
		if(Tool.isStrEmptyOrNull(answer) || !CharUtil.isDateFormat(answer)){
			System.out.println("wrong format in computeYears4Implication");
			return answer;
		}
		
		answer = CharUtil.getDateFormat(answer);
		Calendar ca = Calendar.getInstance();
		int currentYear = ca.get(Calendar.YEAR);
		int targetYear = Integer.parseInt(answer.substring(0, answer.indexOf(CommonConstantName.IS_YEAR)));
		int year = currentYear - targetYear;
		System.out.println("currentYear="+currentYear+", targetYear="+targetYear+", age="+year);
		
		return Integer.toString(year)+CommonConstantName.IS_YEAR;
	}
	
//	// compute the number of year from someone death
//	public static String getPassAwayYearByImplication(String sentence, String entity) {
//		Calendar ca = Calendar.getInstance();
//		int currentYear = ca.get(Calendar.YEAR);
//		String deathInfo = DBProcess.getPropertyValue(entity, "逝世日期");
//		if(Tool.isStrEmptyOrNull(deathInfo)){
//			System.err.println("there is no birth info in entity:"+entity);
//			return "";
//		}
//		
//		deathInfo = deathInfo.substring(0, deathInfo.indexOf("年"));
//		int deathYear = Integer.parseInt(deathInfo);
//		int age = currentYear - deathYear;
//		System.out.println("currentYear="+currentYear+", deathYear="+deathYear+", age="+age);
//		
//		return Integer.toString(age);
//	}
	
	// compute the age
	public static String getAgeByImplication(String sentence, String entity, String key) {
		Calendar ca = Calendar.getInstance();
		int currentYear = ca.get(Calendar.YEAR);
		String label = NLPUtil.getLabelByEntity(entity);
		if(!label.equals(Common.KGDOMAIN_FIGURE)){
			return "";
		}
		String birthInfo = DBProcess.getPropertyValue(label, entity, CommonConstantName.BIRTH_DTAE, key);
		if(Tool.isStrEmptyOrNull(birthInfo) || !CharUtil.isDateFormat(birthInfo)){
			System.err.println("there is no birth info in entity:"+entity);
			return "";
		}
		
		birthInfo = birthInfo.substring(0, birthInfo.indexOf(CommonConstantName.IS_YEAR));
		int birthYear = Integer.parseInt(birthInfo);
		int age = currentYear - birthYear;
		System.out.println("currentYear="+currentYear+", birthYear="+birthYear+", age="+age);
		
		return Integer.toString(age);
	}

	public static String getImplicationAnswer(String sentence, String entity, String prop, String key) {
		System.out.println("getImplicationAnswer: str="+sentence+", prop="+prop);
		if (Tool.isStrEmptyOrNull(entity) || Tool.isStrEmptyOrNull(prop)) {
			System.err.println("entity=" + entity + ", prop=" + prop);
			return "";
		}
		
		String rs = "";

		Class<?> demo = null;
		try {
			demo = Class.forName("com.emotibot.understanding.ImplicationProcess");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String methodName = getMethodName(prop);
			System.out.println("prop="+prop+", methodName="+methodName+", entity="+entity);
			Method method = demo.getMethod(methodName, String.class, String.class, String.class);
			// int year = (int) method.invoke(demo.newInstance());
			rs = (String) method.invoke(demo.newInstance(), sentence, entity, key);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	public static void main(String[] args) {
		ImplicationProcess ip = new ImplicationProcess();
		
		String testStr = "1727年3月31日";
		System.out.println(ip.computeYears4Implication(testStr,"", ""));

	}
}
