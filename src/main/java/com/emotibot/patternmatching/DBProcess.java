package com.emotibot.patternmatching;

import java.util.ArrayList;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
import java.util.List;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.BuildCypherSQL;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.util.Neo4jResultBean;
import com.emotibot.util.Tool;

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

	public static List<String> getPropertyNameSet(String entity, String label) {
		List<String> propSet = new ArrayList<>();
		if(Tool.isStrEmptyOrNull(entity)){
			System.err.println("DBProcess.getPropertyNameSet: input is empty");
			return propSet;
		}
		String query = buildCypherSQLObj.getPropNamebyEntityName(label, entity);
		propSet = conn.getArrayListfromCollection(query);
		System.out.println("in DBProcess, prop name is " + propSet);
		return propSet;
	}
	
	public static List<String> getPropertyNameSet(String entity) {
		List<String> propSet = new ArrayList<>();
		if(Tool.isStrEmptyOrNull(entity)){
			System.err.println("DBProcess.getPropertyNameSet: input is empty");
			return propSet;
		}
		String query = buildCypherSQLObj.getPropNamebyEntityName("", entity);
		propSet = conn.getArrayListfromCollection(query);
		System.out.println("in DBProcess, prop name is " + propSet);
		return propSet;
	}

	public static List<String> getRelationshipSet(String entity) {
		List<String> relationshipSet = new ArrayList<>();
		if(Tool.isStrEmptyOrNull(entity)){
			System.err.println("DBProcess.getRelationshipSet: input is empty");
			return relationshipSet;
		}
		String query = buildCypherSQLObj.getRelationshipByEntityName("", entity);
		relationshipSet = conn.getArrayListfromCollection(query);
		System.out.println("in DBProcess, prop name is " + relationshipSet);
		return relationshipSet;
	}
	
	// get the relationship set in the path from A to B
	public static List<String> getRelationshipTypeInStraightPath(String labelA, String entityA, String labelB, String entityB) {
		List<String> relationshipSet = new ArrayList<>();
		if(Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)){
			System.err.println("DBProcess.getRelationshipTypeInPath: input is empty");
			return relationshipSet;
		}
		String query = buildCypherSQLObj.getRelationshipInStraightPath(labelA, entityA, labelB, entityB);
		relationshipSet = conn.getArrayListfromCollection(query);
		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + relationshipSet);
		return relationshipSet;
	}

	// get the relationship set in the path from A and B to a node C between A and B
	public static String getRelationshipTypeInConvergePath(String labelA, String entityA, String labelB, String entityB) {
		String relationship = "";
		if(Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)){
			System.err.println("DBProcess.getRelationshipTypeInPath: input is empty");
			return relationship;
		}
		String query = buildCypherSQLObj.getRelationshipInConvergePath(labelA, entityA, labelB, entityB);
		relationship = conn.getStringFromDB(query);
		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + relationship);
		return relationship;
	}
	
	// get the relationship set in the path from A and B to a node C between A and B
	public static String getRelationshipTypeInDivergentPath(String labelA, String entityA, String labelB, String entityB) {
		String relationship = "";
		if(Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)){
			System.err.println("DBProcess.getRelationshipTypeInPath: input is empty");
			return relationship;
		}
		String query = buildCypherSQLObj.getRelationshipInDivergentPath(labelA, entityA, labelB, entityB);
		relationship = conn.getStringFromDB(query);
		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + relationship);
		return relationship;
	}
	
	// if there are multiple answers, return the first one.
	public static String getEntityByRelationship(String label, String entity, String relationship) {
		if(Tool.isStrEmptyOrNull(entity) || Tool.isStrEmptyOrNull(relationship)){
			System.err.println("DBProcess.getEntityByRelationship: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.getEntityByRelationship(label, entity, relationship);
		bean = conn.executeCypherSQL(query);
		System.out.println("in DBProcess, it return " + bean.getResult());
		return bean.getResult();
	}
	
	public static String getPropertyValue(String ent, String prop, String label) {
		if(Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(prop)){
			System.err.println("DBProcess.getPropertyValue: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr(label, ent, prop);
		bean = conn.executeCypherSQL(query);
		System.out.println("in DBProcess, it return " + bean.getResult());
		return bean.getResult();
	}
	
	public static String getPropertyValue(String ent, String prop) {
		if(Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(prop)){
			System.err.println("DBProcess.getPropertyValue: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr("", ent, prop);
		bean = conn.executeCypherSQL(query);
		System.out.println("in DBProcess, it return " + bean.getResult());
		return bean.getResult();
	}
	
	
	
	public static void main(String [] args){
		
		String entityA = "The Matrix";
		String entityB = "The Matrix Reloaded";
		
		System.out.println("rs="+DBProcess.getRelationshipTypeInDivergentPath("",entityA,"",entityB));
		
		
		
//		System.out.println("list of prop is: "+getEntityByRelationship("", "test", "ACTS_IN"));
//		System.out.println("list of prop is: "+getPropertyNameSet("Yaoming"));
//		conn.updateQuery(str);
	}

}
