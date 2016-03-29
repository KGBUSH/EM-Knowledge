package com.emotibot.neo4jprocess;

import java.util.HashMap;
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 * Primary Owner: taoliu@emotibot.com.cn
 */
import java.util.Map;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.util.Entity;
import com.emotibot.util.Tool;

public class BuildCypherSQL implements CypherSQLParser {

	/*
	 * // get Entity Query
	 * 
	 * public String getEntity(String label, String name, String value) { String
	 * query = "match (e:" + label + ") where " + name + "=" + value +
	 * " return id(ee) as id, e as entity"; return query; }
	 */

	public String InsertEntityNodeByPageExtractInfo(PageExtractInfo pageInfo) {
		if (pageInfo == null)
			return Common.EMPTY;
		return InsertEntityNode(Common.PERSONLABEL, pageInfo.getName(), pageInfo.getAttr());
	}

	@Override
	public String InsertEntityNode(String Label, String name, Map<String, String> attr) {
		// TODO Auto-generated method stub
		// merge (n:Person{id:11}) set n.名字="姚明",n.position="C" return n
		String query = "";
		if (Tool.isStrEmptyOrNull(Label) || Tool.isStrEmptyOrNull(name) || (attr == null || attr.size() == 0)) {
			System.err.println("InsertEntityNode has invalid input");
		} else {
			query = "merge (" + Common.ResultObj + ":" + Label + "{" + Common.KGNODE_NAMEATRR + ":\"" + name
					+ "\"}) set ";
			for (String key : attr.keySet()) {
				query += Common.ResultObj + "." + key + "=\"" + attr.get(key) + "\",";
			}
			query = query.substring(0, query.length() - 1);
			query += "  return " + Common.ResultObj + ";";

		}
		System.out.println("query in InsertEntityNode is: " + query);
		return query;
	}

	@Override
	public String InsertRelation(Entity entityA, Entity entityB, String relationLabel, Map<String, String> attr) {
		String query = "";
		if (Tool.isStrEmptyOrNull(relationLabel) || entityA == null || entityB == null
				|| Tool.isStrEmptyOrNull(entityA.getLabel()) || Tool.isStrEmptyOrNull(entityB.getLabel())) {
			System.err.println("InsertRelation has invalid input");
		} else {
			query = "match (p:" + entityA.getLabel();
			for (String key : entityA.getProperties().keySet()) {
				query += " {" + key + ":\"" + entityA.getProperties().get(key) + "\"} ";
			}

			query += ") match (q:" + entityB.getLabel();
			for (String key : entityB.getProperties().keySet()) {
				query += " {" + key + ":\"" + entityB.getProperties().get(key) + "\"} ";
			}

			query += ") merge (p)-[r:" + relationLabel + "]->(q) ";
			if (attr != null && attr.size() > 0) {
				query += " set ";
				for (String key : attr.keySet()) {
					query += "r." + key + "=\"" + attr.get(key) + "\",";
				}
				query = query.substring(0, query.length() - 1);
			}
		}
		System.out.println("query in InsertRelation is: " + query);
		return query;
	}

	@Override
	public String FindEntityInfo(String label, String name) {
		String query = "";
		if (Tool.isStrEmptyOrNull(label) || Tool.isStrEmptyOrNull(name)) {
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + name + "\"}) return e."
					+ Common.KG_NODE_FIRST_PARAM_ATTRIBUTENAME + " as " + Common.ResultObj;
		}
		System.out.println("query in FindEnitity is: " + query);
		return query;
	}

	@Override
	public String FindEntityAttr(String label, String name, String attr) {
		String query = "";
		if (Tool.isStrEmptyOrNull(name)) {
			System.err.println("CYPHER: name is null");
			return query;
		}

		if (label.isEmpty()) {
			query = "match (e {" + Common.KGNODE_NAMEATRR + ":\"" + name + "\"}) return e." + attr + " as "
					+ Common.ResultObj;
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + name + "\"}) return e." + attr + " as "
					+ Common.ResultObj;
		}
		System.out.println("query in FindEnitityAttr is: " + query);
		return query;
	}

	@Override
	public String getRelationshipByEntityName(String label, String ent) {
		String query = "";
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("CYPHER: name is null");
			return query;
		}
		
		if (label.isEmpty()) {
			query = "match (e {" + Common.KGNODE_NAMEATRR + ":\"" + ent + "\"})-[r]->() return collect(distinct type(r)) as " + Common.ResultObj;
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + ent + "\"})-[r]->() return collect(distinct type(r)) as "
					+ Common.ResultObj;
		}
		System.out.println("CYPHER of getRelationshipByEntityName: " + query);
		return query;
	}
	
	@Override
	public String getPropNamebyEntityName(String label, String ent) {
		String query = "";
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("CYPHER: name is null");
			return query;
		}
		
		if (label.isEmpty()) {
			query = "match (e {" + Common.KGNODE_NAMEATRR + ":\"" + ent + "\"}) return keys(e) as " + Common.ResultObj;
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + ent + "\"}) return keys(e) as "
					+ Common.ResultObj;
		}
		System.out.println("query in getPropNamebyEntityName is: " + query);
		return query;
	}

	public static void main(String[] args) {
		BuildCypherSQL buildCypherSQL = new BuildCypherSQL();
		Entity entityA = new Entity();
		entityA.setLabel(Common.PERSONLABEL);
		entityA.addProperty("身高", "226cm");
		buildCypherSQL.InsertEntityNode("Person", "姚明", entityA.getProperties());
		Entity entityB = new Entity();
		entityB.addProperty("身高", "190cm");
		entityB.setLabel(Common.PERSONLABEL);
		buildCypherSQL.InsertEntityNode("Person", "叶莉", entityB.getProperties());
		Map<String, String> relation = new HashMap<>();
		relation.put("时间", "2003");

		// instance two entity by lable and name
		Entity a = new Entity(Common.PERSONLABEL, "姚明");
		Entity b = new Entity(Common.PERSONLABEL, "叶莉");

		// note that the relation point from a to b
		buildCypherSQL.InsertRelation(a, b, "老婆", relation);

	}

}
