package com.emotibot.patternmatching;

import java.util.List;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.BuildCypherSQL;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.util.Neo4jResultBean;

public class DBProcess {
	public static EmotibotNeo4jConnection conn = getDBConnection();
	public static BuildCypherSQL buildCypherSQLObj = new BuildCypherSQL();

	public static EmotibotNeo4jConnection getDBConnection() {
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

	public static List<String> getPropertyNameSet(String entity) {
		String query = buildCypherSQLObj.getPropNamebyEntityName(Common.PERSONLABEL, entity);
		List<String> ls = conn.getArrayList(query);
		System.out.println("in PatternMatching, prop name is " + ls);
		return ls;
	}
	
	public static String getPropertyValue(String ent, String prop) {
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr(Common.PERSONLABEL, ent, prop);
		bean = conn.executeCypherSQL(query);
		System.out.println("in pattern Matching, it return " + bean.getResult());
		return bean.getResult();
	}

}
