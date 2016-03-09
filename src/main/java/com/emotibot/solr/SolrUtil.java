package com.emotibot.solr;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.emotibot.common.Common;
import com.emotibot.extractor.BaikeExtractor;
import com.emotibot.extractor.Extractor;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.util.Tool;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

public class SolrUtil {
	private HttpSolrServer server = new HttpSolrServer("http://192.168.1.81:8080/solr/kgtest");

	public void buildIndex() throws SolrServerException, IOException
	{
		Vector<String> files = new Vector<String>();
		files.add("/Users/Elaine/Documents/workspace/html/yaomin");
		files.add("/Users/Elaine/Documents/workspace/html/yaoxinlei");
		files.add("/Users/Elaine/Documents/workspace/html/caiyilin");
		files.add("/Users/Elaine/Documents/workspace/html/linxinru");
		int index=0;
        for(int i=0;i<files.size();i++)
        {
    		String html=Tool.getFileContent(files.get(i));
    		Extractor ex = new BaikeExtractor(html);
			SolrInputDocument doc = new SolrInputDocument();
			PageExtractInfo pageInfo=ex.ProcessPage();
	        System.err.println("Name="+pageInfo.getName());

			doc.addField("cat", pageInfo.getName());
			doc.addField("id", i);
			doc.addField("name", getSegStr(pageInfo.toString()));
//category
			doc.addField("category", pageInfo.toString());

			/*SolrInputDocument doc = new SolrInputDocument();
			doc.addField("cat", "书本 知识");
			doc.addField("id", "我们 是 中国人" + i);
			doc.addField("name", "中国 美国 日本" + i);*/
			server.add(doc);

			//doc.addField("id", "我们 是 中国人" + i);
			//doc.addField("name", "中国 美国 日本" + i);
			server.add(doc);            
        }
        server.commit();
        System.err.println("OK");
		return ;
	}
	public String getSegStr(String str)
	{
		if(Tool.isStrEmptyOrNull(str)) return Common.EMPTY;
		List<Term> termList = HanLP.segment(str);
		System.out.println(termList);
        StringBuffer buffer = new StringBuffer();
        for(Term t : termList)
        {
        	buffer.append(t.word).append(" ");
        }
		return buffer.toString();
		
	}
	
	public static void main(String args[]) throws SolrServerException, IOException
	{
		List<Term> termList = HanLP.segment("商品和服务");
		System.out.println(new SolrUtil().getSegStr("商品和服务"));
		new SolrUtil().buildIndex() ;
	}

}
