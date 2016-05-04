package com.emotibot.weka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.emotibot.util.Tool;
//com.emotibot.weka.SimpleClassifier
public class SimpleClassifier {
	public static Map<String,String> DomainNames;
	public static Map<String,Integer> wordsDomainTime = new HashMap<>();
	public static String OHTHER="other";
	public static void Init()
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
		DomainNames.put("job","");
		DomainNames.put("music","");
		wordsDomainTime = new HashMap<>();
	}
	public static void Train(String fileName)
	{
		Vector<String> lines = Tool.getFileLines(fileName);
		for(String line:lines)
		{
			line=line.replaceAll("Weka:", "");
			String[] arr = line.split("###");
			String tags = arr[0].trim();
			String domain = arr[1].trim();
			//System.err.println(tags+"=="+domain);
			String[] subtags = tags.split(" ");
			for(String tag:subtags)
			{
				tag=tag.trim();
				//System.err.println(tag+"==>");
				if(!DomainNames.containsKey(domain))
				{
					System.err.println("domain="+domain);
					System.exit(0);
				}
                if(tag.trim().length()==0) continue;
                tag=tag+domain;
                if(!wordsDomainTime.containsKey(tag)) wordsDomainTime.put(tag, 1);
                else
                {
                	wordsDomainTime.put(tag, wordsDomainTime.get(tag)+1);
                }
			}
			
		}
	}
    public static String getLabels(String tags)
    {
    	StringBuffer buffer = new StringBuffer();
    	if(Tool.isStrEmptyOrNull(tags)) return OHTHER;
		String[] subtags = tags.split(" ");
		long sum=0;
		 Map<String, Long>  map = new HashMap<>();

		for(String key:DomainNames.keySet())
		{
			sum=0;
			for(String tag:subtags)
			{
				tag=tag+key;
				if(wordsDomainTime.containsKey(tag)) sum+=wordsDomainTime.get(tag);
			}
			map.put(key, sum);
		}
        List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(map.entrySet());  
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {  
            //降序排序  
            @Override  
            public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {  
                //return o1.getValue().compareTo(o2.getValue());  
                return o2.getValue().compareTo(o1.getValue());  
            }

        }); 
        //buffer.append("tags="+tag"");
        int index=0;
        for (Map.Entry<String, Long> mapping : list) {  
        	buffer.append("domain="+mapping.getKey() + " score=" + mapping.getValue()+" ; ");  
        	index++;
        	return mapping.getKey();
        	//if(index>=3) break;
        } 
        if(list==null||list.size()==0)
        {
            buffer.append(OHTHER);
        }
		return buffer.toString();
    }
    
	public static void main(String args[])
	{
		Init();
		Train("arff/wekaNew.txt");
		Vector<String> lines = Tool.getFileLines("arff/wekaNew.txt");
		int all=0;
		int r=0;
		for(String line:lines)
		{
			line=line.replace("Weka:", "");
			String[] arr = line.split("###");
			String tag=arr[0].trim();
			String label = arr[1].trim();
			all++;
			if(label.equals(getLabels(tag))) r++;
			else{
			System.err.println(tag+"  "+label+"  "+getLabels(tag));
			}
		}
		System.err.println(100*(double)r/all);
	}
}
