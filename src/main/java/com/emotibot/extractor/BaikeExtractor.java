package com.emotibot.extractor;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.emotibot.util.CharUtil;
import com.emotibot.util.SentencesUtil;
import com.emotibot.util.Tool;

public class BaikeExtractor extends Extractor {
   private String html="";
	public BaikeExtractor(String html) {
		// TODO Auto-generated constructor stub
		this.html=html;
	}

	@Override
	public PageExtractInfo ProcessPage() {
		// TODO Auto-generated method stub
		PageExtractInfo pageInfo = new PageExtractInfo();
		//System.err.println(html);
		//html=html.replace("</a>", "</a>.");
		Document doc = Jsoup.parse(html);
		String title = doc.title();
		String name = doc.select("dd.lemmaWgt-lemmaTitle-title").select("h1").text();
		//System.out.println("title="+title+"  name="+name);
		pageInfo.setName(name);
/////////////////////////Basic_info
		Elements basicInfo = doc.select("dl.basicInfo-block");
        //System.err.println("attributes="+kv.size());
        Elements attributes = null;
        Elements values = null;
        String attr="";
        String value="";
		for(Element element:basicInfo)
		{
			//element=element.select("sup").remove();
			attributes = element.select("dt.basicInfo-item");
	        values = element.select("dd.basicInfo-item");
	        if(attributes.size()!=values.size()) continue;
	        for(int index=0;index<values.size();index++){
	               attr=attributes.get(index).html().replaceAll("&nbsp;", "");
	               value=values.get(index).text();
	               //System.out.println(attr+"=="+value);
	               pageInfo.addAttr(attr, value);
	        }		
	    }
		//String h1 = doc.body().text();  
		   
        //System.out.println("Afte parsing, Body : " + h1);
///////////////////////////sentence MaoText
		Elements para = doc.select("div.para");
		Map<String,String> MaoTextUrlMap = new HashMap<String,String>();
		Elements MaoTexts=null;
		for(Element element:para)
		{
			element.select("sup").remove();
			element.select("span").remove();
			MaoTextUrlMap.clear();
			MaoTexts=element.select("a");
            if(MaoTexts!=null)
            {
            	for(Element maoTextE : MaoTexts){
            		//System.out.println(e.attr("href")+" ==> "+e.text());
            		String link = maoTextE.attr("href");
            		String word = maoTextE.text().trim();
            		if(link!=null&&(link.startsWith("/view")||link.startsWith("/subview")))
            		{
            			MaoTextUrlMap.put(word, link);
            		}
            	}
            }
            //element
            if(element!=null&&element.text()!=null){
    			if(pageInfo.getFirstPara()==null||pageInfo.getFirstPara().trim().length()==0)
    			{
    				pageInfo.setFirstPara(element.text());
    			}
             for(String sent:SentencesUtil.toSentenceList(element.text()))
             {
            	 sent=sent.trim();
            	 if(sent==null||sent.length()==0||CharUtil.countChineseCharNum(sent)<2) continue;
            	 //if(sent.length()==0||)
            	 Map<String,String> subMaoTextUrls=Tool.WordsInSent(MaoTextUrlMap, sent);
            	 Sentence sentence = new Sentence();
            	 sentence.setSent(sent);
            	 sentence.setMaoText_Url(subMaoTextUrls);
            	 pageInfo.addSentList(sentence);
             }
            }
		}
        return pageInfo;

	}
	//http://baike.baidu.com/link?url=72qLVN_ClKpxrX47ZOyTzAprqBQdLy234q5PbfAk1Y5pVi7a0VJrZAGq1KJ1z61YcYQDnlWrnDvdcm1yVzJBxa
	public static void main(String args[])
	{
		String path="/Users/Elaine/Documents/workspace/html/caiyilin";
		String html=Tool.getFileContent(path);
		Extractor ex = new BaikeExtractor(html);
		System.err.println(ex.ProcessPage().toString());
		
	}

}
