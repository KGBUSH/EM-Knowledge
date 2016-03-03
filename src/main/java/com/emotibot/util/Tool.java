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
import java.util.Map;

import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.log.CommonLogService;
import com.emotibot.log.CommonLogServiceImpl;


public class Tool {
	public static CommonLogService logService = CommonLogServiceImpl.getInstance("Tool");

	public static boolean isStrEmptyOrNull(final String str) {
		if (str == null || str.trim().length() == 0)
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
				if (fileCode.startsWith("GB") && fileCode.contains("2312")) fileCode = "GB2312";
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
	
	public static Map<String,String> WordsInSent(final Map<String,String> MaoUrl,final String sent)
	{
		HashMap<String,String> result = new HashMap<>();
		if(MaoUrl==null||MaoUrl.size()==0) return result;
		if(Tool.isStrEmptyOrNull(sent)) return result;
        for(String key:MaoUrl.keySet())
        {
        	if(sent.contains(key)) result.put(key, MaoUrl.get(key));
        }
		return result;
	}

}
