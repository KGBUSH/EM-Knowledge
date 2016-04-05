package com.emotibot.patternmatching;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.emotibot.config.ConfigKeyName;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */

public class ImplicationProcess {
	private static List<String> implicationWordTable = createImplicationWordTable();
	final Properties properties;
	
	public ImplicationProcess()
	{
		properties = new Properties();
		System.out.println(ConfigKeyName.ImplicationConfigName);
		try {
			properties.load(new FileInputStream(ConfigKeyName.ImplicationConfigName));
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw, true));
			String str = sw.toString();
			System.out.println(str);
		}
		System.out.println("getNeo4jServerIp="+getProperty("年龄"));
		
		
	}
	
	public String getProperty(String prop) {
		// TODO Auto-generated method stub
		if(properties!=null&&properties.containsKey(prop))
			return properties.getProperty(ConfigKeyName.DB_NEO4J_SERVER_IP_NAME);
		return null;
	}


	// create Implication Word Table 
	private static List<String> createImplicationWordTable() {
		List<String> implicationWordSet = new ArrayList<>();
		implicationWordSet.add("年龄"); // TBD: hard code for 4/15
		return implicationWordSet;
	}
	
	public static boolean isImplicationWord(String str) {
		boolean isSW = false;
		if (!str.isEmpty() && implicationWordTable.contains(str)) {
			isSW = true;
		}

		return isSW;
	}
	
	public static String getImplicationAnswer(String entity, String prop){
		
		return "";
	}
	

}
