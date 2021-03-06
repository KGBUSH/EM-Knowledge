package com.emotibot.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.emotibot.common.Common;
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
		name=name.toLowerCase();
		name=name.replace("'", " ");
		name=name.replace("\\", "");
		name=name.replace("/", "");
		name=name.replaceAll("\"", "");
		name=name.replaceAll("“", " ");
		name=name.replaceAll("”", " ");
		name=name.replaceAll("'", "");

		//System.out.println("title="+title+"  name="+name);
		pageInfo.setName(name);
		pageInfo.addAttr(Common.KGNODE_NAMEATRR, name);
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
	               attr=attributes.get(index).html().replaceAll("&nbsp;", "").trim();
	               value=values.get(index).text().trim();
	               ///////////////////////////////////////////////
	            Elements hrefs = values.get(index).select("a");
            	for(Element href : hrefs){
            		String link = href.attr("href");
            		String word = href.text().trim().toLowerCase();////////////
            		if(link==null||link.trim().length()==0) continue;

            		if(!link.startsWith("http://")) link ="http://baike.baidu.com"+link;
            		System.err.println(link+"===>>>"+word);
            		pageInfo.addWordLink(word, link);
            		pageInfo.addAttr_Values(attr, word);
            	}
	               /////////////////////////////////////////////
	               pageInfo.addAttr(attr, value);
	        }		
	    }
		////////////////
		Elements basicInfoPre = doc.select("div[id=slider_relations]");//doc.select("div.star-info-block");
		if(basicInfoPre!=null)
		{
		for(Element element:basicInfoPre)
		{
    		//System.err.println("KKK="+element.text());
			Elements sub=element.select("li");
        	for(Element sub2 : sub){
        		//System.err.println("KKK1="+sub2.html());
        		String link = sub2.select("a").attr("href");
        		String subname = sub2.select("div").attr("title");
        		String relation = sub2.select("div").text();
        		relation=relation.replaceAll(subname, "").trim();
        		System.err.println("KKK2="+link+"  "+subname+"==>"+relation);
        		//===>>>
        		System.err.println(link+"===>>>"+subname);

        		if(link!=null&&link.trim().length()>0)
        		{
            		if(!link.startsWith("http://")) link ="http://baike.baidu.com"+link;
            		pageInfo.addWordLink(subname, link);

        		}
        		if(!Tool.isStrEmptyOrNull(relation)&&!Tool.isStrEmptyOrNull(subname)){
        		   pageInfo.addAttr_Values(relation, subname);
 	               pageInfo.addAttr(relation, subname);
        	    }

        	}
		}
		}
		
		//<div class="open-tag-title">
		//tags
		Elements tags = doc.select("dd[id=open-tag-item]");
		//System.err.println("tags="+tags.html());
        if(tags!=null){
        StringBuffer tagsBuffer = new StringBuffer();
		for(Element element:tags)
		{
    		System.err.println("tag="+tagsBuffer.append(element.select("span[class=taglist]").text()));
		}
		 pageInfo.setTags(tagsBuffer.toString().trim());
         pageInfo.addAttr("tags", tagsBuffer.toString().trim());
        }
		//////////////////
        //pics
        //<div class="summary-pic">
		Elements bigPic = doc.select("div[class=summary-pic]");
		String BigPicUrl=bigPic.select("img").attr("src");
		//if()
		Elements picElements = doc.select("img[class=picture]");
		boolean isBig=true;
		if(picElements!=null&&(BigPicUrl==null||BigPicUrl.trim().length()==0))
		{
		 for(Element element:picElements)
		 {
			String picUrl=element.attr("src").trim();
			String des=element.attr("alt").trim();
			String style=element.attr("style").trim();
			System.err.println("Pic="+des+"  "+style+"  "+picUrl);
			if(picUrl!=null&&picUrl.trim().length()>0){
				BigPicUrl=picUrl;
				isBig=false;
				break;
			}
		 }
		}
		pageInfo.setPic(BigPicUrl);
		System.err.println(title+"###"+isBig+" BigPicUrl="+BigPicUrl);
		if(BigPicUrl!=null&&BigPicUrl.trim().length() >0)  pageInfo.addAttr(Common.KG_NODE_Pic, BigPicUrl);
///////////////////////////////////////////
		 Pattern pattern = Pattern.compile("<a target=_blank href=\"(/[view|subview][^<>\"]{1,100}?htm)\">([^<>\"]{1,10})</a>");
	      Matcher match = pattern.matcher(html);
	      while (match.find()) 
	      {
	    	  String suburl=match.group(1).trim();
	    	  if(!suburl.startsWith("http://")) suburl="http://baike.baidu.com"+suburl;
    		  System.err.println(suburl+"===2>>>"+match.group(2).trim());
	      }
///////////////////////////sentence MaoText
		Elements para = doc.select("div.para");
		Map<String,String> MaoTextUrlMap = new HashMap<String,String>();
		Elements MaoTexts=null;
		StringBuffer buffer = new StringBuffer();
		int time=0;
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
    			   String firstParam=element.text();
    			  // pageInfo.setFirstPara(firstParam);
 	               //pageInfo.addAttr(Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME, firstParam);
    			   time++;
    			   if(time<=2)
    			   {
    				   buffer.append(firstParam);
    			   }
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
		 pageInfo.setFirstPara(buffer.toString());
         pageInfo.addAttr(Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME, buffer.toString());
         pageInfo.addAttr(Common.KG_NODE_FIRST_PARAM_MD5, DigestUtils.md5Hex(buffer.toString()));
         pageInfo.setParamMd5(DigestUtils.md5Hex(buffer.toString()));
         ////
         //<span class="viewTip-fromTitle">卡迪夫城</span>
         Elements tongyiciElement = doc.select("span[class=viewTip-fromTitle]");
         if(tongyiciElement!=null&&tongyiciElement.text()!=null)
         {
        	 String tongyici = tongyiciElement.text().trim();
        	 if(tongyici.length()>0) pageInfo.setTongyici(tongyici.toLowerCase());
         }
         //<a href="/view/10812277.htm" target="_blank">多义词</a>
         Elements duoyiciElement  = doc.select("div[class=polysemantList-header-title]");
         if(duoyiciElement!=null)
         {
        	 boolean duoyici = duoyiciElement.outerHtml().contains("target=\"_blank\">多义词</a>");
        	/* pageInfo.setDuoyici(duoyici);
        	 System.err.println("duoyiciElement.outerHtml()="+duoyiciElement.outerHtml());*/
        	 if(duoyici)
        	 {
        		 String html=duoyiciElement.outerHtml();
        		 //html=html.substring(0, html)
        		 if(html.contains("</b>是一个"))
        		 {
        			 html=html.substring(0, html.indexOf("</b>是一个"));
        			 if(html.contains("<b>"))
        			 {
        				 html=html.substring(html.lastIndexOf("<b>")+"<b>".length(), html.length());
        				 html=html.trim();
        				 pageInfo.setDuoyici(html);
        			 }
        		 }
        	 }
         }
         pageInfo.putArrValuestoAttr();
         /////
        return pageInfo;

	}
	//http://baike.baidu.com/link?url=72qLVN_ClKpxrX47ZOyTzAprqBQdLy234q5PbfAk1Y5pVi7a0VJrZAGq1KJ1z61YcYQDnlWrnDvdcm1yVzJBxa
	public static void main(String args[])
	{
		String path="/Users/Elaine/Documents/workspace/html/xijinping";
		String html=Tool.getFileContent(path);
		Extractor ex = new BaikeExtractor(html);
		PageExtractInfo info = ex.ProcessPage();
		System.err.println(info.toString());
		//System.err.println(info.getWordLink("上海市第二中学"));
		System.err.println(info.GetSynonym());
		System.err.println(info.getFirstPara());
        Map<String,String> map = info.getWordLinkMap();
        for(String key:map.keySet())
        {
    		System.err.println(key+"===>>>"+map.get(key));

        }
        for(String key:info.getAttr_Values().keySet())
        {
    		System.err.println(key+"===2>>>"+info.getAttr_Values().get(key));
        }
        for(String key:info.getAttr().keySet())
        {
    		System.err.println(key+"===3>>>"+info.getAttr().get(key));
        }

		System.err.println("getTongyici.........."+info.getTongyici());
		System.err.println("isDuoyici.........."+info.getDuoyici());
		System.err.println("pic.........."+info.getPic());
		System.err.println(info.getAttrFilterEnStr());

	}
}
