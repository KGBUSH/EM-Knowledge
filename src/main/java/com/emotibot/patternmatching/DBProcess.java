package com.emotibot.patternmatching;

import java.util.ArrayList;
import java.util.HashMap;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
import java.util.List;
import java.util.Map;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.BuildCypherSQL;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.util.Neo4jResultBean;
import com.emotibot.util.Tool;

public class DBProcess {
	// public static EmotibotNeo4jConnection conn = getDBConnection();
	public static BuildCypherSQL buildCypherSQLObj = CypherInit();
	public static Neo4jDBManager neo4jDBManager = DBManagerInit();

	public static BuildCypherSQL CypherInit() {
		System.out.println("CypherInit");
		BuildCypherSQL cypherObj = new BuildCypherSQL();
		ConfigManager cfg = new ConfigManager();
		return cypherObj;
	}

	public static Neo4jDBManager DBManagerInit() {
		System.out.println("DBManagerInit");
		ConfigManager cfg = new ConfigManager();
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(cfg.getNeo4jDriverName());
		neo4jConfigBean.setIp(cfg.getNeo4jServerIp());
		neo4jConfigBean.setPassword(cfg.getNeo4jPasswd());
		neo4jConfigBean.setPort(cfg.getNeo4jServerPort());
		neo4jConfigBean.setUser(cfg.getNeo4jUserName());
		Neo4jDBManager dbm = new Neo4jDBManager(neo4jConfigBean);
		return dbm;
	}

	public static EmotibotNeo4jConnection getDBConnection() {
		return neo4jDBManager.getConnection();
	}

	// get the property name set of an entity
	public static List<String> getPropertyNameSet(String entity, String label) {
		List<String> propSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entity)) {
			System.err.println("DBProcess.getPropertyNameSet: input is empty");
			return propSet;
		}
		String query = buildCypherSQLObj.getPropNamebyEntityName(label, entity);
		EmotibotNeo4jConnection conn = getDBConnection();
		propSet = conn.getArrayListfromCollection(query);
		conn.close();
		System.out.println("in DBProcess, prop name is " + propSet);
		return propSet;
	}

	public static List<String> getPropertyNameSet(String entity) {
		List<String> propSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entity)) {
			System.err.println("DBProcess.getPropertyNameSet: input is empty");
			return propSet;
		}
		String query = buildCypherSQLObj.getPropNamebyEntityName("", entity);
		EmotibotNeo4jConnection conn = getDBConnection();
		propSet = conn.getArrayListfromCollection(query);
		conn.close();
		System.out.println("in DBProcess, prop name is " + propSet);
		return propSet;
	}

	public static List<String> getRelationshipSet(String entity) {
		List<String> relationshipSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entity)) {
			System.err.println("DBProcess.getRelationshipSet: input is empty");
			return relationshipSet;
		}
		String query = buildCypherSQLObj.getRelationshipByEntityName("", entity);
		EmotibotNeo4jConnection conn = getDBConnection();
		relationshipSet = conn.getArrayListfromCollection(query);
		conn.close();
		System.out.println("in DBProcess, prop name is " + relationshipSet);
		return relationshipSet;
	}

	// get the relationship set in the path from A to B
	public static List<String> getRelationshipTypeInStraightPath(String labelA, String entityA, String labelB,
			String entityB) {
		List<String> relationshipSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)) {
			System.err.println("DBProcess.getRelationshipTypeInPath: input is empty");
			return relationshipSet;
		}
		String query = buildCypherSQLObj.getRelationshipInStraightPath(labelA, entityA, labelB, entityB);
		EmotibotNeo4jConnection conn = getDBConnection();
		relationshipSet = conn.getArrayListfromCollection(query);
		conn.close();
		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + relationshipSet);
		return relationshipSet;
	}

	// get the relationship set in the path from A and B to a node C between A
	// and B
	public static List<List<String>> getRelationshipTypeInConvergePath(String labelA, String entityA, String labelB,
			String entityB) {
		List<List<String>> rsSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)) {
			System.err.println("DBProcess.getRelationshipTypeInPath: input is empty");
			return rsSet;
		}

		List<String> list = new ArrayList<>();
		list.add(Common.RelationName);
		list.add(Common.RelationType);

		String query = buildCypherSQLObj.getRelationshipInConvergePath(labelA, entityA, labelB, entityB);
		EmotibotNeo4jConnection conn = getDBConnection();
		rsSet = conn.getListSet(query, list);
		conn.close();

		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + rsSet);
		return rsSet;
	}

	// get the relationship set in the path from A and B to a node C between A
	// and B
	public static List<List<String>> getRelationshipTypeInDivergentPath(String labelA, String entityA, String labelB,
			String entityB) {
		List<List<String>> rsSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)) {
			System.err.println("DBProcess.getRelationshipTypeInPath: input is empty");
			return rsSet;
		}

		List<String> list = new ArrayList<>();
		list.add(Common.RelationName);
		list.add(Common.RelationType);

		String query = buildCypherSQLObj.getRelationshipInDivergentPath(labelA, entityA, labelB, entityB);
		EmotibotNeo4jConnection conn = getDBConnection();
		rsSet = conn.getListSet(query, list);
		conn.close();
		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + rsSet);
		return rsSet;
	}

	// if there are multiple answers, return the first one.
	public static String getEntityByRelationship(String label, String entity, String relationship) {
		if (Tool.isStrEmptyOrNull(entity) || Tool.isStrEmptyOrNull(relationship)) {
			System.err.println("DBProcess.getEntityByRelationship: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.getEntityByRelationship(label, entity, relationship);
		EmotibotNeo4jConnection conn = getDBConnection();
		bean = conn.executeCypherSQL(query);
		conn.close();
		System.out.println("in DBProcess, it return " + bean.getResult());
		return bean.getResult();
	}

	public static String getPropertyValue(String ent, String prop, String label) {
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(prop)) {
			System.err.println("DBProcess.getPropertyValue: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr(label, ent, prop);
		EmotibotNeo4jConnection conn = getDBConnection();
		bean = conn.executeCypherSQL(query);
		conn.close();
		System.out.println("in DBProcess, it return " + bean.getResult());
		return bean.getResult();
	}

	public static String getPropertyValue(String ent, String prop) {
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(prop)) {
			System.err.println("DBProcess.getPropertyValue: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr("", ent, prop);
		EmotibotNeo4jConnection conn = getDBConnection();
		bean = conn.executeCypherSQL(query);
		conn.close();
		System.out.println("in DBProcess, it return " + bean.getResult());
		return bean.getResult();
	}

	public static String getEntityLabel(String ent) {
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("DBProcess.getEntityLabel: input is empty");
			return "";
		}
		String query = buildCypherSQLObj.getLabelByEntity(ent);
		EmotibotNeo4jConnection conn = getDBConnection();
		List<String> list = conn.getArrayListfromCollection(query);
		conn.close();
		System.out.println("in DBProcess, getEntityLabel " + list);
		return list.get(0);
	}

	// get the property value Map of an entity
	// [(身高,226),(老婆，叶莉)]
	public static Map<String, Object> getEntityPropValueMap(String label, String entity) {
		Map<String, Object> entityMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(entity)) {
			System.err.println("DBProcess.getPropertyValueMap: input is empty");
			return null;
		}
		String query = buildCypherSQLObj.getEntity(label, entity);
		EmotibotNeo4jConnection conn = getDBConnection();
		entityMap = conn.getEntityMap(query);
		conn.close();

		// Map<String, String> valuePropMap = new HashMap<>();
		// for(Map.Entry<String, Object> entry : entityMap.entrySet()){
		// valuePropMap.put(entry.getValue().toString(), entry.getKey());
		// }

		System.out.println("in DBProcess, getPropertyValueMap = " + entityMap);
		return entityMap;
	}

	public static void main(String[] args) {
		System.out.println("label=" + DBProcess.getEntityLabel("姚明"));

		String entityA = "The Matrix";
		String entityB = "The Matrix Reloaded";

		String query = "match ({Name:\"霍建华\"})-[r1]->(n)<-[r2]-({Name:\"赵丽颖\"}) where type(r1)=type(r2) return n.Name as relationName, type(r1) as relationType";

		List<String> list = new ArrayList<>();
		list.add(Common.RelationName);
		list.add(Common.RelationType);

		EmotibotNeo4jConnection conn = getDBConnection();
		System.out.println("rs=" + conn.getListSet(query, list));
		conn.close();

		// System.out.println("list of prop is: "+getEntityByRelationship("",
		// "test", "ACTS_IN"));
		// System.out.println("list of prop is:
		// "+getPropertyNameSet("Yaoming"));
		// conn.updateQuery(str);
	}

}
