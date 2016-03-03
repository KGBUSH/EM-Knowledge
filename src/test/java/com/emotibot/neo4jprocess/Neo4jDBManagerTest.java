package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
 		for(String f : files)
		{
			String html=Tool.getFileContent(f);
			Extractor ex = new BaikeExtractor(html);
			PageExtractInfo pageInfo=ex.ProcessPage();
			//System.err.println(ex.ProcessPage().toString());
			String query=BuildCypherSQLObj.InsertEntityNodeByPageExtractInfo(pageInfo);
			System.err.println(query);
			Neo4jResultBean bean=conn.executeCypherSQL(query);
			System.err.println(bean.toString());


		}
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
		SQLExample();
	}

}
