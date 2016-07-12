package com.emotibot.understanding;

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
//	public static Neo4jDBManager neo4jDBManager = DBManagerInit();
	public static final Neo4jConfigBean neo4jConfigBean = ConfigBeanInit();
	
	public static Neo4jConfigBean ConfigBeanInit(){
		ConfigManager cfg = new ConfigManager();
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(cfg.getNeo4jDriverName());
		neo4jConfigBean.setIp(cfg.getNeo4jServerIp());
		neo4jConfigBean.setPassword(cfg.getNeo4jPasswd());
		neo4jConfigBean.setPort(cfg.getNeo4jServerPort());
		neo4jConfigBean.setUser(cfg.getNeo4jUserName());
		return neo4jConfigBean;
	}

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
//		return neo4jDBManager.getConnection();
		return new EmotibotNeo4jConnection(neo4jConfigBean);
	}

	public static void freeDBConnection(EmotibotNeo4jConnection conn) {
//		neo4jDBManager.freeConnection(conn);
		conn.close();
	}

	// get the property name set of an entity
	public static List<String> getPropertyNameSet(String label, String entity, String key) {
		if(Tool.isStrEmptyOrNull(label)){
			System.err.println("label is empty");
		}
		List<String> propSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entity)) {
			System.err.println("DBProcess.getPropertyNameSet: input is empty");
			return propSet;
		}
		key = (Tool.isStrEmptyOrNull(key)) ? "" : key;
		label = (Tool.isStrEmptyOrNull(label)) ? "" : label;
		
		String query = buildCypherSQLObj.getPropNamebyEntityName(label, entity, key);
		EmotibotNeo4jConnection conn = getDBConnection();
		propSet = conn.getArrayListfromCollection(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess, prop name is " + propSet);
		return propSet;
	}
	
	
	// get relation set of a entity
	public static List<String> getRelationshipSet(String label, String entity, String key) {
		if(Tool.isStrEmptyOrNull(label)){
			System.err.println("label is empty");
		}
		List<String> relationshipSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entity)) {
			System.err.println("DBProcess.getRelationshipSet: input is empty");
			return relationshipSet;
		}
		key = (Tool.isStrEmptyOrNull(key)) ? "" : key;
		label = (Tool.isStrEmptyOrNull(label)) ? "" : label;
		
		String query = buildCypherSQLObj.getRelationshipByEntityName(label, entity, key);
		EmotibotNeo4jConnection conn = getDBConnection();
		relationshipSet = conn.getArrayListfromCollection(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess, prop name is " + relationshipSet);
		return relationshipSet;
	}

	// get the relationship set in the path from A to B
	public static List<String> getRelationshipTypeInStraightPath(String labelA, String entityA, String labelB,
			String entityB, int step) {
		if(Tool.isStrEmptyOrNull(labelA) || Tool.isStrEmptyOrNull(labelB)){
			System.err.println("label is empty");
		}
		List<String> relationshipSet = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)) {
			System.err.println("DBProcess.getRelationshipTypeInPath: input is empty");
			return relationshipSet;
		}
		String query = buildCypherSQLObj.getRelationshipInStraightPath(labelA, entityA, labelB, entityB, step);
		EmotibotNeo4jConnection conn = getDBConnection();
		relationshipSet = conn.getArrayListfromCollection(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + relationshipSet);
		return relationshipSet;
	}

	// get the relationship set in the path from A and B to a node C between A
	// and B
	public static List<List<String>> getRelationshipTypeInConvergePath(String labelA, String entityA, String labelB,
			String entityB) {
		if(Tool.isStrEmptyOrNull(labelA) || Tool.isStrEmptyOrNull(labelB)){
			System.err.println("label is empty");
		}
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
		freeDBConnection(conn);

		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + rsSet);
		return rsSet;
	}

	// get the relationship set in the path from A and B to a node C between A
	// and B
	public static List<List<String>> getRelationshipTypeInDivergentPath(String labelA, String entityA, String labelB,
			String entityB) {
		if(Tool.isStrEmptyOrNull(labelA) || Tool.isStrEmptyOrNull(labelB)){
			System.err.println("label is empty");
		}
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
		freeDBConnection(conn);
		System.out.println("in DBProcess.getRelationshipTypeInPath, rs = " + rsSet);
		return rsSet;
	}

	// if there are multiple answers, return the first one.
	public static Map<String, Object> getEntityByRelationship(String label, String entity, String relationship, String key) {
		if(Tool.isStrEmptyOrNull(label)){
			System.err.println("label is empty");
		}
		Map<String, Object> entityMap = new HashMap<>();
		if (Tool.isStrEmptyOrNull(entity) || Tool.isStrEmptyOrNull(relationship)) {
			System.err.println("DBProcess.getEntityByRelationship: input is empty");
			return entityMap;
		}
		String query = buildCypherSQLObj.getEntityByRelationship(label, entity, relationship, key);
		EmotibotNeo4jConnection conn = getDBConnection();
//		bean = conn.executeCypherSQL(query);
		entityMap = conn.getEntityMap(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess:getEntityByRelationship, it return " + entityMap);
		return entityMap;
	}

	// use public static String getPropertyValue(String label, String ent, String prop, String key)
	@Deprecated
	public static String getPropertyValue(String label, String ent, String prop) {
		if(Tool.isStrEmptyOrNull(label)){
			System.err.println("label is empty");
		}
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(prop)) {
			System.err.println("DBProcess.getPropertyValue: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr(label, ent, prop);
		EmotibotNeo4jConnection conn = getDBConnection();
		bean = conn.executeCypherSQL(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess:getPropertyValue, it return " + bean.getResult());
		return bean.getResult();
	}
	
	public static String getPropertyValue(String label, String ent, String prop, String key) {
		if(Tool.isStrEmptyOrNull(label)){
			System.err.println("label is empty");
		}
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(prop)) {
			System.err.println("DBProcess.getPropertyValue: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr(label, ent, prop, key);
		EmotibotNeo4jConnection conn = getDBConnection();
		bean = conn.executeCypherSQL(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess:getPropertyValue, it return " + bean.getResult());
		return bean.getResult();
	}

	// use public static String getPropertyValue(String label, String ent, String prop, String key)
	@Deprecated
	public static String getPropertyValue(String ent, String prop) {
		System.err.println("label is empty");
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(prop)) {
			System.err.println("DBProcess.getPropertyValue: input is empty");
			return "";
		}
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.FindEntityAttr("", ent, prop);
		EmotibotNeo4jConnection conn = getDBConnection();
		bean = conn.executeCypherSQL(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess, it return " + bean.getResult());
		return bean.getResult();
	}
	
	// get introduction info of an entity
	public static String getEntityIntroduction(String ent, String label) {
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("DBProcess.getEntityIntroduction: input is empty");
			return "";
		}
		
		Neo4jResultBean bean = null;
		String query = buildCypherSQLObj.Find1stLevelEntityAttr(label, ent, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
		EmotibotNeo4jConnection conn = getDBConnection();
		bean = conn.executeCypherSQL(query);
		
		if(bean == null || bean.getResult().isEmpty()){
			query = buildCypherSQLObj.FindEntityAttr(label, ent, Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME);
			bean = conn.executeCypherSQL(query);
			System.out.println("in DBProcess.getEntityIntroduction, get the second level entity " + bean.getResult());
		} else {
			System.out.println("in DBProcess.getEntityIntroduction, get the first level entity " + bean.getResult());
		}
		
		freeDBConnection(conn);
		return bean.getResult();
	}
	
	// get property list from an entity with type = 1 first
	public static List<String> getEntityPropertyList(String ent, String label){
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(label)) {
			System.err.println("DBProcess.getEntityPropertyList: input is empty");
			return new ArrayList<String>();
		}
		
		List<String> propSet = new ArrayList<String>();
		String query = buildCypherSQLObj.Find1stLevelEntityProperty(label, ent);
		EmotibotNeo4jConnection conn = getDBConnection();
		propSet = conn.getArrayListfromCollection(query);
		
		if(propSet == null || propSet.size() == 0){
			query = buildCypherSQLObj.FindEntityProperty(label, ent);
			propSet = conn.getArrayListfromCollection(query);
			System.out.println("in DBProcess.getEntityPropertyList, get the second level entity property " + propSet);
		} else {
			System.out.println("in DBProcess.getEntityPropertyList, get the type = 1 entity property" + propSet);
		}
		
		freeDBConnection(conn);
		return propSet;
	}
	
	//get relation list from an entity with type = 1 first
	public static List<String> getEntityRelationList(String ent, String label){
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(label)) {
			System.err.println("DBProcess.getEntityPropertyList: input is empty");
			return new ArrayList<String>();
		}
		
		List<String> propSet = new ArrayList<String>();
		String query = buildCypherSQLObj.Find1stLevelEntityRelation(label, ent);
		EmotibotNeo4jConnection conn = getDBConnection();
		propSet = conn.getArrayListfromCollection(query);
		if(propSet == null || propSet.size() == 0){
			query = buildCypherSQLObj.FindEntityRelation(label, ent);
			propSet = conn.getArrayListfromCollection(query);
			System.out.println("in DBProcess.getEntityRelationList, get the sencond level entity relation " + propSet);
		}else {
			System.out.println("in DBProcess.getEntityRelationList, get the type = 1 entity relation " + propSet);
		}
		freeDBConnection(conn);
		return propSet;
	}

	// get the label of an entity, if there are more than one label, return the first one
	public static String getEntityLabel(String ent) {
		System.err.println("label is empty");
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("DBProcess.getEntityLabel: input is empty");
			return "";
		}
		String query = buildCypherSQLObj.getLabelByEntity(ent);
		EmotibotNeo4jConnection conn = getDBConnection();
		List<String> list = conn.getArrayListfromCollection(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess, getEntityLabel " + list);
		if(list == null || list.isEmpty()){
			System.err.println("there is no node in DB");
			return "";
		} else {
			return list.get(0);
		}
	}
	
	// return the list of label of the input entity
	public static List<String> getEntityLabelList(String ent) {
		System.err.println("label is empty");
		List<String> list = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("DBProcess.getEntityLabel: input is empty");
			return list;
		}
		String query = buildCypherSQLObj.getLabelListByEntity(ent);
		EmotibotNeo4jConnection conn = getDBConnection();
		list = conn.getArrayListfromCollection(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess, getEntityLabelList " + list);
		if(list == null || list.isEmpty()){
			System.err.println("there is no node in DB");
			return new ArrayList<>();
		} else {
			return list;
		}
	}
	
	// return the list of label of the input entity
	public static List<String> getKeyListbyEntity(String ent, String label) {
		List<String> list = new ArrayList<>();
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("DBProcess.getEntityKeyList: input is empty");
			return list;
		}
		String query = buildCypherSQLObj.getKeyListByEntity(ent, label);
		EmotibotNeo4jConnection conn = getDBConnection();
		list = conn.getArrayListfromCollection(query);
		freeDBConnection(conn);
		System.out.println("in DBProcess, getKeyListbyEntity " + list);
		if(list == null || list.isEmpty()){
			System.err.println("there is no node in DB");
			return new ArrayList<>();
		} else {
			return list;
		}
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
		freeDBConnection(conn);

		// Map<String, String> valuePropMap = new HashMap<>();
		// for(Map.Entry<String, Object> entry : entityMap.entrySet()){
		// valuePropMap.put(entry.getValue().toString(), entry.getKey());
		// }

		System.out.println("in DBProcess, getPropertyValueMap = " + entityMap);
		return entityMap;
	}

	public static void main(String[] args) {
		List<String> string = getPropertyNameSet("figure","李白","");
		System.out.println(string);
		for (int i = 0; i < 10; i++) {
			System.out.println("label=" + DBProcess.getEntityLabel("姚明"));
		}

		System.exit(0);

		String entityA = "The Matrix";
		String entityB = "The Matrix Reloaded";

		String query = "match ({Name:\"霍建华\"})-[r1]->(n)<-[r2]-({Name:\"赵丽颖\"}) where type(r1)=type(r2) return n.Name as relationName, type(r1) as relationType";

		List<String> list = new ArrayList<>();
		list.add(Common.RelationName);
		list.add(Common.RelationType);

		EmotibotNeo4jConnection conn = getDBConnection();
		System.out.println("rs=" + conn.getListSet(query, list));
		freeDBConnection(conn);

		// System.out.println("list of prop is: "+getEntityByRelationship("",
		// "test", "ACTS_IN"));
		// System.out.println("list of prop is:
		// "+getPropertyNameSet("Yaoming"));
		// conn.updateQuery(str);
	}

}
