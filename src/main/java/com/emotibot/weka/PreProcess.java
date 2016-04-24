package com.emotibot.weka;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

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
			f.write("‘"+tags+"’,"+domain+"\r\n");
		}
		f.close();
	}
		
	
	public static void ProduceArffNum(String dataFile) throws IOException
	{
		if(Tool.isStrEmptyOrNull(dataFile)) return ;
		
	}

	public static void main(String args[]) throws IOException
	{
		ProduceArff("weka.txt");
	}

}
