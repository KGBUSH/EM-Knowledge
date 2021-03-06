package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.emotibot.Debug.Debug;
import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.extractor.BaikeExtractor;
import com.emotibot.extractor.Extractor;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.util.Neo4jResultBean;
import com.emotibot.util.Tool;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
public class Neo4jDBManagerTest {

	public static void BuildKG()
	{
		Vector<String> files = new Vector<String>();
		files.add("/Users/Elaine/Documents/workspace/html/yaomin");
		files.add("/Users/Elaine/Documents/workspace/html/yaoxinlei");
		files.add("/Users/Elaine/Documents/workspace/html/caiyilin");
		files.add("/Users/Elaine/Documents/workspace/html/linxinru");
		EmotibotNeo4jConnection conn = getEmotibotNeo4jConnection();
		BuildCypherSQL BuildCypherSQLObj = new BuildCypherSQL();
        List<String> list = new ArrayList<String>();
 		for(String f : files)
		{
 			String html=Tool.getFileContent(f);
 			Extractor ex = new BaikeExtractor(html);
 			PageExtractInfo pageInfo=ex.ProcessPage();

 			//for(int index=0;index<1000;index++)
 			//{
 				
			//pageInfo.setName(String.valueOf(index));
			String query=BuildCypherSQLObj.InsertEntityNodeByPageExtractInfo(pageInfo);
			query=query.substring(0, query.indexOf("return"));

			System.err.println(query);
			list.add(query);
			//Neo4jResultBean bean=conn.executeCypherSQL(query);
			//System.err.println(bean.toString());
 			//}


		}
// 		conn.updateQueryBatch(list);
 		conn.close();
 		conn=null;
	}
	public static void SQLExample()
	{
		EmotibotNeo4jConnection conn = getEmotibotNeo4jConnection();
		BuildCypherSQL BuildCypherSQLObj = new BuildCypherSQL();
        //实体基本信息
		String query=BuildCypherSQLObj.FindEntityInfo(Common.PERSONLABEL, "林心如");
		Neo4jResultBean bean=conn.executeCypherSQL(query);
		System.err.println(bean.toString());
		System.err.println();
		//实体的某个属性
		query=BuildCypherSQLObj.FindEntityAttr(Common.PERSONLABEL, "姚明","运动项目");
		bean=conn.executeCypherSQL(query);
		System.err.println(bean.toString());
		System.err.println();
 		conn.close();
 		conn=null;

	}
	public static EmotibotNeo4jConnection getEmotibotNeo4jConnection()
	{
		ConfigManager cfg = new ConfigManager();
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(cfg.getNeo4jDriverName());
		neo4jConfigBean.setIp(cfg.getNeo4jServerIp());
		neo4jConfigBean.setPassword(cfg.getNeo4jPasswd());
		neo4jConfigBean.setPort(cfg.getNeo4jServerPort());
		neo4jConfigBean.setUser(cfg.getNeo4jUserName());
		Neo4jDBManager neo4jDBManager = new Neo4jDBManager(neo4jConfigBean);
        return neo4jDBManager.getConnection();
	}
	public static void main(String args[]) throws SQLException {
		//BuildKG();
		//SQLExample();
		//EmotibotNeo4jConnection con=getEmotibotNeo4jConnection();
		//Vector<String> sqls = Tool.getFileLines("sql");
		//int index=0;
		/*for(String sql:sqls)
		{
			sql=sql.substring(0, sql.indexOf("return"));
			System.err.println(index+++"  "+sql);

			System.err.println(con.updateQuery(sql));

		}*/
		//List<String> list = new ArrayList<String>();
		//for(int index=0;index<100;index++)
		//{
			
		//}
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	 //   System.err.println(sdf.format(System.currentTimeMillis()));
		//Debug.printDebug(uniqueID, 3, "knowledge", cuBean.toString());

	    //Debug.printDebug("111", 1, "liutao", sdf.format(System.currentTimeMillis()));
	    int ret = Debug.printDebug("123", 1, "test", "hello!");
	    System.out.printf("ret: %d\n", ret);
	}

}
