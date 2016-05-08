package com.emotibot.weka;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.emotibot.util.Tool;

public class TagCommon {
	
	public static Map<String,String> DomainNames;
	public static Map<String,Integer> mapIndex = new HashMap<>();
	public static Map<String,Integer> DomainNum = new HashMap<>();
    public static String CommonTarffStr="";
    public final static String other="other";
	public static Vector<String> features = new Vector<>();
	public static Map<String,Integer> featuresNum = new HashMap<>();

	static
	{
		DomainNames = new HashMap<>();
		
		DomainNames.put("TV_series","");
		DomainNames.put("anime","");
		DomainNames.put("catchword","");
		DomainNames.put("college","");
		DomainNames.put("computer_game","");
		DomainNames.put("cosmetics","");
		DomainNames.put("delicacy","");
		DomainNames.put("digital_product","");
		DomainNames.put("figure","");
		DomainNames.put("major","");
		DomainNames.put("movie","");
		DomainNames.put("novel","");
		DomainNames.put("pet","");
		DomainNames.put("sports","");
		DomainNames.put("tourism","");
		DomainNames.put("economy","");
		DomainNames.put("medical_treatment","");
		//DomainNames.put("job","");
		DomainNames.put("music","");
		features=Tool.getFileLines("arff/feture");
		for(String f:features)
		{
			String arr[]= f.split("==>");
			featuresNum.put(arr[0].trim(), Integer.valueOf(arr[1].trim()));
		}
	}
	
	public static void main(String args[])
	{
		for(String key:DomainNames.keySet())
		{
			System.err.println("cat num | grep "+key+" | head -20 >> 1");
		}
	}


}
