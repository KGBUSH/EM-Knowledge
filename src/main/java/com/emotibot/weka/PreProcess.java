package com.emotibot.weka;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import com.emotibot.util.Tool;

public class PreProcess {

	public static void ProduceArff(String dataFile) throws IOException
	{
		if(Tool.isStrEmptyOrNull(dataFile)) return ;
		FileWriter f = new FileWriter("tag.arff",true);
		StringBuffer buffer = new StringBuffer();
		buffer.append("@attribute class {");
		for(String key:TagCommon.DomainNames.keySet())
		{
			buffer.append(key+",");
		}
		String classes = buffer.toString().trim();
		classes=classes.substring(0, classes.length()-1);
		classes=classes+"}";
		f.write(classes+"\r\n");
		//@data
		f.write("@data"+"\r\n");

		Vector<String> lines = Tool.getFileLines(dataFile);
		for(String line:lines)
		{
			line=line.replaceAll("Weka:", "");
			if(line.contains("^M")) line=line.replaceAll("^M", "");
			String[] arr = line.split("###");
			String tags = arr[0].trim();
			String domain = arr[1].trim();
			f.write("'"+tags+"',"+domain+"\r\n");
		}
		f.close();
	}
		
	
	public static void ProduceArffNum(String dataFile) throws IOException
	{
		if(Tool.isStrEmptyOrNull(dataFile)) return ;
		Vector<String> lines = Tool.getFileLines(dataFile);
		Vector<String> words = new Vector<String>();
		for(String line:lines)
		{
			line=line.replaceAll("Weka:", "");
			if(line.contains("^M")) line=line.replaceAll("^M", "");
			String[] arr = line.split("###");
			String tags = arr[0].trim();
			String domain = arr[1].trim();
			for(String tag:tags.split(" "))
			{
				tag=tag.trim();
				System.err.println("tag="+tag);
				if(!words.contains(tag)) words.add(tag);
			}
		}
		
		int index=1;

		for(String w:words)
		{
			TagCommon.mapIndex.put(w, index);
			index++;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("@relation 'tag classification'"+"\r\n");
		StringBuffer buffer2 = new StringBuffer();

		for(String key:TagCommon.DomainNames.keySet())
		{
			buffer2.append(key+",");
		}
		buffer.append("@attribute class {");

		String classes = buffer2.toString().trim();
		classes=classes.substring(0, classes.length()-1);
		classes=classes+"}";
		buffer.append(classes+"\r\n");

		for(String w:words)
		{
			buffer.append("@attribute "+w+" numeric"+"\r\n");
		}

		buffer.append("\r\n");
		buffer.append("@data "+"\r\n");
		buffer.append("\r\n");
        if(TagCommon.CommonTarffStr.length()==0)
        {
        	TagCommon.CommonTarffStr=buffer.toString();
        }
		for(String line:lines)
		{
			line=line.replaceAll("Weka:", "");
			if(line.contains("^M")) line=line.replaceAll("^M", "");
			String[] arr = line.split("###");
			String tags = arr[0].trim();
			String domain = arr[1].trim();
			if(!TagCommon.DomainNum.containsKey(domain))
			{
				TagCommon.DomainNum.put(domain, 1);
			}
			else
			{
				TagCommon.DomainNum.put(domain, TagCommon.DomainNum.get(domain)+1);
			}

			 Map<Integer, Integer>  map = new HashMap<>();
			 for(String tag:tags.split(" "))
				{
				 map.put(TagCommon.mapIndex.get(tag), 1);
				}
		        List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(map.entrySet());  
		        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {  
		            //降序排序  
		            @Override  
		            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {  
		                return o1.getKey().compareTo(o2.getKey());  
		            }
		        }); 

		        String newLine="{0 "+domain;
		        for (Map.Entry<Integer, Integer> mapping : list) {  
					newLine+=","+mapping.getKey()+" 1";
		        } 

				newLine+="}";
				buffer.append(newLine+"\r\n");
		}
		FileWriter f = new FileWriter("tag.arff");
        f.write(buffer.toString());
        f.close();
        for(String key:TagCommon.DomainNum.keySet())
        {
        	System.err.println(key+"="+TagCommon.DomainNum.get(key));
        }
	}
	public static void main(String args[]) throws IOException
	{
		ProduceArffNum("weka.txt");
	}

}
