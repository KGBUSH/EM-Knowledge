/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.extractor.BaikeExtractor;
import com.emotibot.extractor.Extractor;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class SolrUtil {
	private HttpSolrClient server = null;
	public static final String Name = "KG_Name";
	public static final String Attr = "KG_Attr";
	public static final String Value = "KG_Value";
	public static final String AttrValue = "KG_Attr_Value";
	public static final String Info = "KG_Info";

	// new HttpSolrServer("http://192.168.1.81:8080/solr/kgtest");
	public SolrUtil() {
		if (server == null) {
			ConfigManager cf = new ConfigManager();
			String ip=cf.getIndexSolrServerIp();
			int port = cf.getIndexSolrServerPort();
			String solrName=cf.getIndexSolrServerSolrName();
			SystemDefaultHttpClient httpClient = new SystemDefaultHttpClient();
			server = new HttpSolrClient("http://"+ip+":"+port+"/solr/"+solrName,httpClient);
			server.setConnectionTimeout(10 * 1000);
			server.setFollowRedirects(false);
			server.setAllowCompression(true);
			server.setMaxRetries(10);
		}
	}

	public SolrUtil(String ip,int port,String solrName) {
		if (server == null) {
			SystemDefaultHttpClient httpClient = new SystemDefaultHttpClient();
			server = new HttpSolrClient("http://"+ip+":"+port+"/solr/"+solrName,httpClient);
			server.setConnectionTimeout(10 * 1000);
			server.setFollowRedirects(false);
			server.setAllowCompression(true);
			server.setMaxRetries(10);
		}
	}

	public boolean Commit() {
		try {
			if (server != null){
			   // server.optimize();
				server.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean deleteAllIndex() {
		try {
			if (server != null) {
				server.deleteByQuery("*:*");
				server.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public boolean addDoc(PageExtractInfo pageInfo) {
		if (pageInfo == null) return false;
		try {
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", pageInfo.getName());
			doc.addField(Name, pageInfo.getName());
			doc.addField(Attr, pageInfo.getAttrStr());
			doc.addField(Value, pageInfo.getValueStr());
			doc.addField(AttrValue, pageInfo.getAttrValueStr());
			doc.addField(Info, pageInfo.toSolrString());
			server.add(doc);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean addDoc(SolrInputDocument doc) {
		if (doc == null)  return false;
		try {
			server.add(doc);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    public List<String> Search(Solr_Query SolrQuery)
    {
    	List<String> result = new ArrayList<String>();
    	try{
    	String query = SolrQuery.getQuery();
    	if(Tool.isStrEmptyOrNull(query)) return result;
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.set("rows", 10);
        solrQuery.set("fl", "*,score");
        solrQuery.set("q", query);
        solrQuery.set("df", "KG_Name");
        //solrQuery.s
        QueryResponse response=server.query(solrQuery);
        SolrDocumentList docList = response.getResults();
        for (SolrDocument doc : docList) {
            //String info = (String)doc.getFieldValue(Info);
            //System.err.println("info="+info);
            //String attr = (String)doc.getFieldValue(Attr);
            //System.err.println("attr="+attr);
           // String value = (String)doc.getFieldValue(Value);
            //System.err.println("value="+value);
            String name = doc.getFieldValue(Name).toString();
            if(name!=null&&name.trim().length()>0)
            {
            	name=name.replace("[", "");
            	name=name.replace("]", "");
            }
            System.out.println(""+name);
            //return name;
            if(name!=null&&name.trim().length()>0){
            result.add(name);
            }
        }
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return result;
    }
	public static void main(String args[]) throws SolrServerException, IOException, InterruptedException {
		/*SolrUtil solr = new SolrUtil();
    	Vector<String> files = new Vector<String>();
		 files.add("/Users/Elaine/Documents/workspace/html/yaomin");
		 files.add("/Users/Elaine/Documents/workspace/html/yaoxinlei");
		 files.add("/Users/Elaine/Documents/workspace/html/caiyilin");
		 files.add("/Users/Elaine/Documents/workspace/html/linxinru"); int
		 index = 0; 
		 for (int i = 0; i < files.size(); i++) { 
		 String html =Tool.getFileContent(files.get(i)); 
		 Extractor ex = new BaikeExtractor(html); 
		 PageExtractInfo pageInfo = ex.ProcessPage();
		 solr.addDoc(pageInfo);
		 String name = pageInfo.getName();
		 for(int j=0;j<100;j++)
		 {
			 pageInfo.setName(name+j);
			 solr.addDoc(pageInfo);
				System.err.println("i,j="+i+"  "+j);

		 }
		    long t1=System.currentTimeMillis();
			 solr.Commit(); 
			 System.err.println("OK");
	    	long t2=System.currentTimeMillis();
			System.err.println("time="+(t2-t1));
           Thread.sleep(10*1000);
		 }*/
		Solr_Query obj = new Solr_Query();
		obj.setFindEntity(true);
		//obj.addEntity("姚明");
		obj.addEntity("菩萨");

		//obj.addWord("姚明");
		obj.addWord("丈夫");
		obj.addWord("忉利天");
		SolrUtil solr = new SolrUtil();
    	long t1=System.currentTimeMillis();
		solr.Search(obj);
    	long t2=System.currentTimeMillis();
		System.err.println("time="+(t2-t1));

		return;
	}

}
