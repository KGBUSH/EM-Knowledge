package com.emotibot.offline.dataprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.emotibot.util.Tool;

public class dataprocess {
	public static String flag1="Tongyici1";
	public static String flag2="Tongyici2";
	public static String flag3="###";

    public static String getUrlMd5(String url)
    {
    	if(Tool.isStrEmptyOrNull(url)) return "";
    	if(url.startsWith("http://baike.baidu.com/")) url=url.replace("http://baike.baidu.com/", "");
    	if(url.contains(".htm")) url=url.substring(0, url.lastIndexOf(".htm"));
    	return url;
    }
	public static void main(String args[])
	{
		String path="";
		Vector<String> lines= Tool.getFileLines(path);
		Map<String,List<String>> md5Words = new HashMap<>();
		for(String line:lines)
		{
			if(Tool.isStrEmptyOrNull(line)) continue;
			if(line.startsWith("Tongyici1"))
			{
				line=line.replace("Tongyici1:", "").trim();
				String[] arr = line.split(flag3);
				if(arr.length==3)
				{
					String md5 = getUrlMd5(arr[2].trim());
					if(!md5Words.containsKey(md5))
					{
						md5Words.put(md5, new ArrayList<String>());
					}
					md5Words.get(md5).add(arr[0].trim());
					//if(md5Words.get(md5).contains(arr[1].trim())) 
						md5Words.get(md5).add(arr[1].trim());

				}
			}
			if(line.startsWith("Tongyici2"))
			{
				line=line.replace("Tongyici2:", "").trim();
				String[] arr = line.split(flag3);
				if(arr.length==2)
				{
					String md5 = getUrlMd5(arr[1].trim());
					if(!md5Words.containsKey(md5))
					{
						md5Words.put(md5, new ArrayList<String>());
						md5Words.get(md5).add(arr[0].trim());
					}
					else
					{
						//if(!md5Words.get(md5).contains(arr[0].trim())) 
							md5Words.get(md5).add(arr[0].trim());
					}

				}

			}

		}
		for(String key:md5Words.keySet())
		{
			if(md5Words.get(key).size()>1)
			{
				System.err.println(key+" "+md5Words.get(key));
			}
		}
	}

}
