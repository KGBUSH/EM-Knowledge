/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.log.CommonLogService;
import com.emotibot.log.CommonLogServiceImpl;

public class Tool {
	public static CommonLogService logService = CommonLogServiceImpl.getInstance("Tool");

	public static boolean isStrEmptyOrNull(final String str) {
		if (str == null || CharUtil.trim(str).length() == 0)
			return true;
		return false;
	}

	public static String getFileContent(String fileName) {
		StringBuffer buffer = new StringBuffer();
		if (isStrEmptyOrNull(fileName))
			return Common.EMPTY;
		else {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String line = "";
				while ((line = dis.readLine()) != null) {
					buffer.append(line.trim());
				}
			} catch (Exception e) {
				logService.log(e.getMessage());
				return Common.EMPTY;
			}
			return buffer.toString();
		}
	}

	public static Vector<String> getFileLines(String fileName) {
		Vector<String> result = new Vector<String>();
		if (isStrEmptyOrNull(fileName))
			return result;
		else {
			try {
				BytesEncodingDetect s = new BytesEncodingDetect();
				String fileCode = BytesEncodingDetect.nicename[s.detectEncoding(new File(fileName))];
				if (fileCode.startsWith("GB") && fileCode.contains("2312"))
					fileCode = "GB2312";
				FileInputStream fis = new FileInputStream(fileName);
				InputStreamReader read = new InputStreamReader(fis, fileCode);
				BufferedReader dis = new BufferedReader(read);
				String line = "";
				while ((line = dis.readLine()) != null) {
					result.add(line.trim());
				}
			} catch (Exception e) {
				logService.log(e.getMessage());
				return result;
			}
			return result;
		}
	}

	public static Map<String, String> WordsInSent(final Map<String, String> MaoUrl, final String sent) {
		HashMap<String, String> result = new HashMap<>();
		if (MaoUrl == null || MaoUrl.size() == 0)
			return result;
		if (Tool.isStrEmptyOrNull(sent))
			return result;
		for (String key : MaoUrl.keySet()) {
			if (sent.contains(key))
				result.put(key, MaoUrl.get(key));
		}
		return result;
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}

	public static boolean isEnglish(String str) {
		return str.matches("^[a-zA-Z]*");
	}

	// add a space for each Chinese character in the sentence.
	public static String insertSpace4ChineseCharacter(String str) {
		String temp = "";
		for (int i = 0; i < str.length(); i++) {
			if (isChinese(str.charAt(i))) {
				temp += str.charAt(i) + " ";
			} else {
				temp += str.charAt(i);
			}
		}

		return temp;
	}

	public static String insertSpace2Chinese(String s) {
		int tail = 0;
		char[] temp = new char[s.length() * 2];
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isChinese(c)) {
				temp[tail] = c;
				temp[tail + 1] = ' ';
				tail += 2;
			} else {
				temp[tail] = c;
				tail++;
			}
		}
		return new String(temp);
	}

	public static String insertSpace2Sentence(String s) {
		int tail = 0;
		char[] temp = new char[s.length() * 2];
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isChinese(c) || isEnglish(Character.toString(c))) {
				temp[tail] = c;
				temp[tail + 1] = ' ';
				tail += 2;
			} else {
				temp[tail] = c;
				tail++;
			}
		}
		return new String(temp);
	}

	public static String removeSpaceFromChinese(String s) {
		Boolean bCn = false;
		int tail = 0;
		char[] temp = new char[s.length()];
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isChinese(c))
				bCn = true;
			else if (c != ' ')
				bCn = false;
			if (c != ' ') {
				temp[tail] = c;
				tail++;
			} else if (!bCn) {
				temp[tail] = c;
				tail++;
			}
		}
		return new String(temp);
	}
	
	public static String removeSpaceInChinese(String str) {
		String[] splitStr = str.split(" ");
		String rs = "";
		for (String s : splitStr) {
			rs += s;
		}

		// System.out.println("input is " + str + ", output is " + rs);
		return rs;
	}

	public static String getLongestStringInArray(String[] arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}

		int index = 0;
		int length = -1;
		for (int i = 0; i < arr.length; i++) {
			if (length < arr[i].length()) {
				length = arr[i].length();
				index = i;
			}
		}
		return arr[index];
	}
	
	//input result1, result2
	//output rewrite = "\"["+answer+"],[" + template+"]\"";
	public static String combineTwoResult(String result1, String result2){
		StringBuilder result = new StringBuilder();
		if(result1.contains("[")&&result1.contains("]")){
			//remove the " end of the result1.
			result1 = result1.replaceAll("\"", "");
			result.append("\"").append(result1).append(",[").append(result2).append("]\"");
//			result ="\""+ result1+",[" + result2+"]\"";
		}else {
			result.append("\"[").append(result1).append("],[").append(result2).append("]\"");
//			result = "\"["+result1+"],[" + result2+"]\"";
		}
		
		return result.toString();
	}

}
