package com.emotibot.dictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.emotibot.common.Common;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.util.CharUtil;

public class GeneratePMRefFiles {

	@Deprecated
	public static void getEntityInfoInList() {
		try {
			BufferedReader entityURL = new BufferedReader(new FileReader(Common.UserDir + "/txt/temp/target_url_all_entity"));
			BufferedReader entityAll = new BufferedReader(new FileReader(Common.UserDir + "/txt/temp/duo2"));
			BufferedWriter out = new BufferedWriter(new FileWriter(Common.UserDir + "/txt/temp/DYC_Default.txt"));
			
			Map<String, String> refMap = new HashMap<>();
			String line = null;
			int count = 0;
			// Duoyici:1995-2005夏至未至###文学作品 文学书籍 小说作品 小说 书籍###http://baike.baidu.com/subview/114707/11099385.htm
			while ((line = entityAll.readLine()) != null) {
				line = CharUtil.trim(line).toLowerCase();
				if (line.isEmpty())
					continue;
				
				count++;
				String url = line.substring(line.lastIndexOf("###")+3);
				url = CharUtil.trim(url);
				if(count < 5)
					System.out.println("url="+url);
				refMap.put(url, line);
			}
			
			Set<String> refSet = new HashSet<>();
			line = null;
			count = 0;
			int count2 = 0;
			while ((line = entityURL.readLine()) != null) {
				line = CharUtil.trim(line).toLowerCase();
				if (line.isEmpty())
					continue;
				count++;
				String url = line.substring(line.lastIndexOf("http://"));
				url = CharUtil.trim(url);
				if(count < 5)
					System.out.println("2 url="+url);
				
				if(refMap.keySet().contains(url)){
					out.write(refMap.get(url) + "\r\n");
				} else {
					String name = line.substring(0, line.lastIndexOf(" http://"));
					name = CharUtil.trim(name);
					refSet.add(name);
					if(count2++ < 50)
						System.out.println("1 name="+name);
				}
			}
			
			entityAll.close();
			
			entityAll = new BufferedReader(new FileReader(Common.UserDir + "/txt/temp/duo2"));
			line = null;
			count = 0;
			// Duoyici:1995-2005夏至未至###文学作品 文学书籍 小说作品 小说 书籍###http://baike.baidu.com/subview/114707/11099385.htm
			while ((line = entityAll.readLine()) != null) {
				line = CharUtil.trim(line).toLowerCase();
				if (line.isEmpty())
					continue;
				
				count++;
				String name = line.substring(8, line.indexOf("###"));
				name = CharUtil.trim(name);
				if(count < 5)
					System.out.println("2 name="+name);
				if(refSet.contains(name)){
					out.write(line + "\r\n");
				}
			}
			
			entityURL.close();
			entityAll.close();
			out.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
		
	public static void main(String [] args){
		getEntityInfoInList();
	}
		

}
