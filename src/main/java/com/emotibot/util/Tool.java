package com.emotibot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.emotibot.common.BytesEncodingDetect;
import com.emotibot.common.Common;
import com.emotibot.log.CommonLogService;
import com.emotibot.log.CommonLogServiceImpl;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
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

}
