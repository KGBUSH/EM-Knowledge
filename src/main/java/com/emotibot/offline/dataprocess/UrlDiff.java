package com.emotibot.offline.dataprocess;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.emotibot.Debug.Debug;
import com.emotibot.util.Tool;

public class UrlDiff {
	public static void main(String args[]) throws IOException {
		//
		//
		//
		String urlhaveFile="";//已有的url文件名
		String urlnewFile="";//新发现的二级地址的文件名
		String saveFile="";//去重后的二级地址的文件名

		Map<String, String> UrlHave = new HashMap<String, String>();
		
		Vector<String> UrlAllVec = Tool.getFileLines(urlnewFile);
		Vector<String> UrlHaveVec = Tool.getFileLines(urlhaveFile);
		for (String w : UrlHaveVec) {
			if (w != null && w.trim().length() > 0) UrlHave.put(w.trim(), "");
		}
		FileWriter f = new FileWriter(saveFile);
		for (String w : UrlAllVec) {
			if (!UrlHave.containsKey(w)){
				System.err.println(""+w);
				f.write(w+"\r\n");
			}

		}
		f.close();
		System.err.println("END");

	}
	/*public static void main2(String args[]) throws IOException {
		Vector<String> lines = Tool.getFileLines("node100wan");
		FileWriter f = new FileWriter("UrlKeyParamKeyMap.txt");

		for (String line : lines) {
			if (line!= null && line.trim().length() > 0) {
		        Pattern pattern = Pattern.compile("\\{key:\"([^\"].*?)\"\\}.*result.urlkey=\"([^\"].*?)\"");
		            Matcher match = pattern.matcher(line);
		            if(match.find()) {
		            	String urlkey=match.group(2).trim();
		            	String key=match.group(1).trim();
		            	System.err.println(urlkey+"###"+key);
		            	f.write(urlkey+"###"+key+"\r\n");
		              }
		            else
		            {
		            	System.err.println(line);
		            }
			}
		}
		f.close();
    	System.err.println("End");
	}*/

}
