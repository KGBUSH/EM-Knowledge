package com.emotibot.log;

import java.io.BufferedWriter;
import java.io.FileWriter;

import com.emotibot.common.Common;

public class LogService {
	
	public static void printLog(String id, String file, String log){
		try{
			BufferedWriter exceptionLog = new BufferedWriter(new FileWriter(Common.UserDir + "/log/exception.txt", true));
			exceptionLog.write("id="+id+", filename="+file+", log="+log+"\r\n");
			exceptionLog.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
