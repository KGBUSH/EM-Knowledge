package com.emotibot.neo4jprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.emotibot.config.ConfigManager;
import com.emotibot.extractor.BaikeExtractor;
import com.emotibot.extractor.Extractor;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.util.Tool;

public class DBTest {
	
	public static void main(String args[])
	{
		int index = Integer.valueOf(args[0].trim());
		
		
		ConfigManager cfg = new ConfigManager();
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(cfg.getNeo4jDriverName());
		neo4jConfigBean.setIp(cfg.getNeo4jServerIp());
		neo4jConfigBean.setPassword(cfg.getNeo4jPasswd());
		neo4jConfigBean.setPort(cfg.getNeo4jServerPort());
		neo4jConfigBean.setUser(cfg.getNeo4jUserName());
		Neo4jDBManager neo4jDBManager = new Neo4jDBManager(neo4jConfigBean);
		EmotibotNeo4jConnection conn= neo4jDBManager.getConnection();
		
		    String file = "yaoming";
			String html=Tool.getFileContent(file);
			Extractor ex = new BaikeExtractor(html);
			PageExtractInfo pageInfo=ex.ProcessPage();
			String name = pageInfo.getName();
			BuildCypherSQL BuildCypherSQLObj = new BuildCypherSQL();
            List<String> list = new ArrayList<String>();
			for(int i=0;i<index;i++)
			{
				pageInfo.setName(name+i);
				pageInfo.addAttr("Name", pageInfo.getName());
				String query = BuildCypherSQLObj.InsertEntityNodeByPageExtractInfo(pageInfo);
    			if(query.contains("return")) query = query.substring(0, query.lastIndexOf("return"));
    			query=query.replaceAll("result", "result"+list.size());
                list.add(query);
                if(list.size()>100)
                {
                	boolean result=conn.updateQueryBatch(list);
					System.err.println("result="+result +"index="+index);
					list.clear();
                }

			}
            if(list.size()>0)
            {
            	boolean result=conn.updateQueryBatch(list);
				System.err.println("result="+result);
				list.clear();
            }
            conn.close();
            conn=null;
			System.err.println("End!");


	}

}
