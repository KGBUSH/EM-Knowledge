package com.emotibot.offline.dataprocess;

import java.io.FileWriter;
import java.io.IOException;
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
    	//if(url.startsWith("http://baike.baidu.com/")) url=url.replace("http://baike.baidu.com/", "");
    	if(url.contains(".htm")) url=url.substring(0, url.lastIndexOf(".htm"));
    	return url;
    }
    public static String transform(String url)
    {
    	//url = url.replaceAll("[　*| *| *|//s*]*", ""); 
    	//url=url.replaceAll("\u200B", "");
    	url=url.replaceAll(" ", "");

        url=url.trim();
        url=url.toLowerCase();
        return url;
    }

	public static void main(String args[]) throws IOException
	{
		String path="Tong3";
		Vector<String> lines= Tool.getFileLines(path);
		Map<String,Map<String,String>> md5Words = new HashMap<>();
		for(String line:lines)
		{
			if(Tool.isStrEmptyOrNull(line)) continue;
			if(line.startsWith("Tongyici1"))
			{
				line=line.replace("Tongyici1:", "").trim();
				String[] arr = line.split(flag3);
				//System.err.println(arr.length);
				String md5 = getUrlMd5(arr[2].trim());

				if(arr.length==3||arr.length==4)
				{
					//String md5 = getUrlMd5(arr[2].trim());
					if(!md5Words.containsKey(md5))
					{
						md5Words.put(md5, new HashMap<String,String>());
					}
					arr[0]=transform(arr[0]);
					arr[1]=transform(arr[1]);
					if(arr[0].trim().length()>0) md5Words.get(md5).put(arr[0].trim(),"");
					if(arr[1].trim().length()>0){
					if(!md5Words.get(md5).containsKey(arr[1].trim()))  md5Words.get(md5).put(arr[1].trim(),"");
					}
				}
				if(arr.length==4)
				{
					String l=arr[3].trim();
					l=l.replaceAll(",", ",");
					l=l.replaceAll("，", ",");
					l=l.replaceAll("、", ",");
                    String arr2[] = l.split(",");
                    for(String w:arr2)
                    {
                    	w=transform(w);
                    	if(w.equals("null")) continue;
    					if(!md5Words.containsKey(md5))
    					{
    						md5Words.put(md5, new HashMap<String,String>());
    					}
                        if(!Tool.isStrEmptyOrNull(w)) md5Words.get(md5).put(w,"");
                    }
				}
			}
			if(line.startsWith("Tongyici2"))
			{
				line=line.replace("Tongyici2:", "").trim();
				String[] arr = line.split(flag3);
				if(arr.length==2)
				{
					String md5 = getUrlMd5(arr[1].trim());
					arr[0]=transform(arr[0]);
					if(arr[0].trim().length()>0){
					if(!md5Words.containsKey(md5))
					{
						md5Words.put(md5, new HashMap<String,String>());
						md5Words.get(md5).put(arr[0].trim(),"");
					}
					else
					{
						if(!md5Words.get(md5).containsKey(arr[0].trim()))  md5Words.get(md5).put(arr[0].trim(),"");
					}
                    }
				}

			}

		}
		FileWriter f  = new FileWriter("word");
		for(String key:md5Words.keySet())
		{
			if(md5Words.get(key).size()>1)
			{
				f.write(key+"###");
				for(String w:md5Words.get(key).keySet())
				{
					f.write(w.trim()+"###");
				}
				f.write("###");
				f.write("\r\n");
			}
		}
		f.close();
		System.err.println("End");
	}

}
