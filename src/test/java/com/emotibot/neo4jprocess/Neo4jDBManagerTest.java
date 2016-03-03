package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.emotibot.config.ConfigManager;

public class Neo4jDBManagerTest {

	public static void main(String args[]) throws SQLException {
		ConfigManager cfg = new ConfigManager();
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(cfg.getNeo4jDriverName());
		neo4jConfigBean.setIp(cfg.getNeo4jServerIp());
		neo4jConfigBean.setPassword(cfg.getNeo4jPasswd());
		neo4jConfigBean.setPort(cfg.getNeo4jServerPort());
		neo4jConfigBean.setUser(cfg.getNeo4jUserName());
		Neo4jDBManager neo4jDBManager = new Neo4jDBManager(neo4jConfigBean);
		// neo4jDBManager.g
		System.out.println("Step1");
		System.out.println(neo4jDBManager.Info());

		EmotibotNeo4jConnection conn = neo4jDBManager.getConnection();
		System.out.println("Step2");
		System.out.println(neo4jDBManager.Info());
		EmotibotNeo4jConnection conn2 = neo4jDBManager.getConnection();
		System.out.println(neo4jDBManager.Info());
		neo4jDBManager.freeConnection(conn2);
		System.out.println(neo4jDBManager.Info());
		
		
		 Statement stmt = conn.getNeo4jConnnection().createStatement();
		 ResultSet rs = stmt.executeQuery(
		 "MATCH (ee:Person) RETURN id(ee) as id ,ee;");
	
		 Map<String, String> map = new HashMap<String, String>(); 
		 while(rs.next()) 
		 { 
	     System.out.println("test");
		 Map<String, Object> e = (Map<String, Object>) rs.getObject("ee"); 
	     System.out.println(rs.getObject("id"));
		 for(String key : e.keySet()) { System.out.println(key + "=" +e.get(key)); }
		 }

	}

}
