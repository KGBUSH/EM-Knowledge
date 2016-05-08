package com.emotibot.weka;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.emotibot.patternmatching.NLPProcess;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.seg.common.Term;
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
	public static Map<String,Integer> getWords(String line)
	{
		Map<String,Integer> map = new HashMap<String,Integer>();
		if(Tool.isStrEmptyOrNull(line)) return map;
		String[] arr = line.split(" ");
		for(String word:arr)
		{
			if(Tool.isStrEmptyOrNull(word)) continue;
			word=word.trim();
			if(word.length()<=3){
				if(!map.containsKey(word)) map.put(word, 1);
				else{
					map.put(word, map.get(word)+1);
				}
			}
			else
			{
				List<Term> list=NLPProcess.getSegWord(word);
                for(Term w:list)
                {
                	String ww=w.word.trim();
    				if(!map.containsKey(ww)) map.put(ww, 1);
    				else{
    					map.put(ww, map.get(ww)+1);
    				}
                }
			}
		}
		/*String[] arr = line.split(" ");
		for(String word:arr)
		{
			map.put(word, 1);
		}*/
		return map;

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
				/*List<Term> list=NLPProcess.getSegWord(tag);

                for(Term t:list){
                tag=t.word;
                tag=tag+domain;
                if(!wordsDomainTime.containsKey(tag)) wordsDomainTime.put(tag, 1);
                else
                {
                	wordsDomainTime.put(tag, wordsDomainTime.get(tag)+1);
                }
                }*/
                Map<String,Integer> map = getWords(tag);
                for(String word:map.keySet())
                {
                	int num=map.get(word);
                	word=word+domain;
                	System.err.println(word);
                    if(!wordsDomainTime.containsKey(word)) wordsDomainTime.put(word, 1);
                    else
                    {
                    	wordsDomainTime.put(word, wordsDomainTime.get(word)+1);
                    }

                }
			}
			
		}
	}
    public static String getLabels(String tags)
    {
    	StringBuffer buffer = new StringBuffer();
    	if(Tool.isStrEmptyOrNull(tags)) return OHTHER;
        Map<String,Integer> submap = getWords(tags);

		long sum=0;
		 Map<String, Long>  map = new HashMap<>();

		for(String key:DomainNames.keySet())
		{
			sum=0;
			for(String tag:submap.keySet())
			{
				tag=tag+key;
				if(wordsDomainTime.containsKey(tag)) sum+=wordsDomainTime.get(tag);
				if(TagCommon.featuresNum.containsKey(tag))
				{
					//System.err.println(tag+"===========>>>");
					sum+=TagCommon.featuresNum.get(tag);
				}
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
        	buffer.append("###"+mapping.getKey());  
        	index++;
        	//return mapping.getKey();
        	if(index>=1) break;
        } 
        if(list==null||list.size()==0)
        {
            buffer.append(OHTHER);
        }
		return buffer.toString();
    }
    
	public static void main(String args[]) throws IOException
	{
		Init();
		Train("arff/w");
		Vector<String> lines = Tool.getFileLines("arff/w");
		int all=0;
		int r=0;
        FileWriter f2 = new FileWriter("record");

		for(String line:lines)
		{
			line=line.replace("Weka:", "");
			String[] arr = line.split("###");
			String tag=arr[0].trim();
			String label = arr[1].trim();
			all++;
			if(getLabels(tag).contains(label)) r++;
			else{
			System.err.println("Weka:"+tag+"###"+label+"###"+getLabels(tag));
			f2.write("Weka:"+tag+"###"+label+"###"+getLabels(tag)+"\r\n");
			}
		}
		f2.close();
		System.err.println(100*(double)r/all);
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(wordsDomainTime.entrySet());  
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {  
            //降序排序  
            @Override  
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {  
                //return o1.getValue().compareTo(o2.getValue());  
                return o2.getValue().compareTo(o1.getValue());  
            }

        }); 			    
        FileWriter f = new FileWriter("num");


		for(Map.Entry<String, Integer> mapping : list)
		{
			//System.err.println(mapping.getKey()+"==>"+mapping.getValue());
			f.write(mapping.getKey()+"==>"+mapping.getValue()+"\r\n");
		}
       f.close();
	}
}
