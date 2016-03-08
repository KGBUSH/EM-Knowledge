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
	private HttpSolrServer server = null;
	public static final String Name = "KG_Name";
	public static final String Attr = "KG_Attr";
	public static final String Value = "KG_Value";
	public static final String AttrValue = "KG_Attr_Value";
	public static final String Info = "KG_Info";

	// new HttpSolrServer("http://192.168.1.81:8080/solr/kgtest");
	public SolrUtil() {
		if (server == null)
			server = new HttpSolrServer("http://192.168.1.81:8080/solr/kgtest");
	}

	public boolean Commit() {
		try {
			if (server != null) server.commit();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
   public boolean addDoc(PageExtractInfo pageInfo)
   {
	   if(pageInfo==null) return true;
	   try
	   {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", pageInfo.getName());
		doc.addField(Name, pageInfo.getName());
		doc.addField(Attr, pageInfo.getAttrStr());
		doc.addField(Value, pageInfo.getValueStr());
		doc.addField(AttrValue, pageInfo.getAttrValueStr());
		doc.addField(Info, pageInfo.toSolrString());
        server.add(doc);
	   }catch(Exception e)
	   {
		   e.printStackTrace();
		   return false;
	   }
	   return true;
   }
	public void buildIndex() throws SolrServerException, IOException {
	}

	public String getSegStr(String str) {
		if (Tool.isStrEmptyOrNull(str))
			return Common.EMPTY;
		List<Term> termList = HanLP.segment(str);
		System.out.println(termList);
		StringBuffer buffer = new StringBuffer();
		for (Term t : termList) {
			buffer.append(t.word).append(" ");
		}
		return buffer.toString();

	}

	public static void main(String args[]) throws SolrServerException, IOException {
		SolrUtil solr = new SolrUtil();
		Vector<String> files = new Vector<String>();
		files.add("/Users/Elaine/Documents/workspace/html/yaomin");
		files.add("/Users/Elaine/Documents/workspace/html/yaoxinlei");
		files.add("/Users/Elaine/Documents/workspace/html/caiyilin");
		files.add("/Users/Elaine/Documents/workspace/html/linxinru");
		int index = 0;
		for (int i = 0; i < files.size(); i++) {
			String html = Tool.getFileContent(files.get(i));
			Extractor ex = new BaikeExtractor(html);
			PageExtractInfo pageInfo = ex.ProcessPage();
			solr.addDoc(pageInfo);
		}
		solr.Commit();
		System.err.println("OK");
		return;
	}

}
