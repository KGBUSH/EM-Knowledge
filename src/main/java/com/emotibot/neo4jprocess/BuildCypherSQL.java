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

	// get Entity Query

	@Override
	public String getEntity(String label, String entity) {
		String query = "";
		if (Tool.isStrEmptyOrNull(entity)) {
			System.err.println("CYPHER.getEntity: name is null");
			return query;
		}

		if (label.isEmpty()) {
			query = "match (e{" + Common.KGNODE_NAMEATRR + ":\"" + entity + "\"}) return e as " + Common.ResultObj;
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":" + entity + "}) return e as "
					+ Common.ResultObj;
		}

		System.out.println("query in getEntity is: " + query);
		return query;
	}

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
			query = "merge (" + Common.ResultObj  + ":"+Label+"{" + Common.KG_NODE_FIRST_PARAM_MD5 + ":\"" + name
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
			String name1 = "p";
			String name2 = "q";
			query = "match (" + name1 + ":" + entityA.getLabel();
			for (String key : entityA.getProperties().keySet()) {
				query += " {" + key + ":\"" + entityA.getProperties().get(key) + "\"} ";
			}

			query += ") match (" + name2 + ":" + entityB.getLabel();
			for (String key : entityB.getProperties().keySet()) {
				query += " {" + key + ":\"" + entityB.getProperties().get(key) + "\"} ";
			}

			query += ") merge (" + name1 + ")-[r:" + relationLabel + "]->(" + name2 + ") ";
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
		System.out.println("CYPHER in FindEnitityAttr is: " + query);
		return query;
	}

	@Override
	public String getEntityByRelationship(String label, String entity, String relation) {
		String query = "";
		if (Tool.isStrEmptyOrNull(entity) || Tool.isStrEmptyOrNull(relation)) {
			System.err.println("CYPHER: entity or relation is null");
			return query;
		}

		if (label.isEmpty()) {
			query = "match (e {" + Common.KGNODE_NAMEATRR + ":\"" + entity + "\"})-[r:" + relation
					+ "]-(m) return m.Name as " + Common.ResultObj;
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + entity + "\"})-[r:" + relation
					+ "]-(m) return m.Name as " + Common.ResultObj;
		}
		System.out.println("CYPHER of getEntityByRelationship: " + query);
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
			query = "match (e {" + Common.KGNODE_NAMEATRR + ":\"" + ent
					+ "\"})-[r]->() return collect(distinct type(r)) as " + Common.ResultObj;
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + ent
					+ "\"})-[r]->() return collect(distinct type(r)) as " + Common.ResultObj;
		}
		System.out.println("CYPHER of getRelationshipByEntityName: " + query);
		return query;
	}

	@Override
	public String getRelationshipInStraightPath(String labelA, String entityA, String labelB, String entityB,
			int step) {
		String query = "";
		if (Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)) {
			System.err.println("CYPHER.getRelationshipInPath: name is null");
			return query;
		}

		// set the length of path not exceed 5 for 4/15
		if (labelA.isEmpty()) {
			query = "match ({" + Common.KGNODE_NAMEATRR + ":\"" + entityA + "\"})-[r*1.." + step + "]->";
		} else {
			query = "match (:" + labelA + "{" + Common.KGNODE_NAMEATRR + ":\"" + entityA + "\"})-[r*1.." + step + "]->";
		}

		if (labelB.isEmpty()) {
			query += "({" + Common.KGNODE_NAMEATRR + ":\"" + entityB + "\"}) UNWIND r as x return collect(type(x)) as "
					+ Common.ResultObj;
		} else {
			query += "(:" + labelB + "{" + Common.KGNODE_NAMEATRR + ":\"" + entityB
					+ "\"}) UNWIND r as x return collect(type(x)) as " + Common.ResultObj;
		}

		System.out.println("CYPHER of getRelationshipInPath: " + query);
		return query;
	}

	@Override
	public String getRelationshipInConvergePath(String labelA, String entityA, String labelB, String entityB) {
		String query = "";
		if (Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)) {
			System.err.println("CYPHER.getRelationshipInConvergePath: name is null");
			return query;
		}

		// set the length of path not exceed 5 for 4/15
		if (labelA.isEmpty()) {
			query = "match ({" + Common.KGNODE_NAMEATRR + ":\"" + entityA + "\"})-[r1]->(n)<-[r2]-";
		} else {
			query = "match (:" + labelA + "{" + Common.KGNODE_NAMEATRR + ":\"" + entityA + "\"})-[r1]->(n)<-[r2]-";
		}

		if (labelB.isEmpty()) {
			query += "({" + Common.KGNODE_NAMEATRR + ":\"" + entityB + "\"}) ";
		} else {
			query += "(:" + labelB + "{" + Common.KGNODE_NAMEATRR + ":\"" + entityB + "\"}) ";
		}

		query += "where type(r1)=type(r2) return n.Name as " + Common.RelationName + ", type(r1) as "
				+ Common.RelationType;

		System.out.println("CYPHER of getRelationshipInConvergePath: " + query);
		return query;
	}

	@Override
	public String getRelationshipInDivergentPath(String labelA, String entityA, String labelB, String entityB) {
		String query = "";
		if (Tool.isStrEmptyOrNull(entityA) || Tool.isStrEmptyOrNull(entityB)) {
			System.err.println("CYPHER.getRelationshipInDivergentPath: name is null");
			return query;
		}

		// set the length of path not exceed 5 for 4/15
		if (labelA.isEmpty()) {
			query = "match ({" + Common.KGNODE_NAMEATRR + ":\"" + entityA + "\"})<-[r1]-(n)-[r2]->";
		} else {
			query = "match (:" + labelA + "{" + Common.KGNODE_NAMEATRR + ":\"" + entityA + "\"})<-[r1]-(n)-[r2]->";
		}

		if (labelB.isEmpty()) {
			query += "({" + Common.KGNODE_NAMEATRR + ":\"" + entityB + "\"}) ";
		} else {
			query += "(:" + labelB + "{" + Common.KGNODE_NAMEATRR + ":\"" + entityB + "\"}) ";
		}

		query += "where type(r1)=type(r2) return n.Name as " + Common.RelationName + ", type(r1) as "
				+ Common.RelationType;

		System.out.println("CYPHER of getRelationshipInDivergentPath: " + query);
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

	@Override
	public String getLabelByEntity(String ent) {
		String query = "";
		if (Tool.isStrEmptyOrNull(ent)) {
			System.err.println("CYPHER: name is null");
			return query;
		}
		query = "match (e {" + Common.KGNODE_NAMEATRR + ":\"" + ent + "\"}) return labels(e) as " + Common.ResultObj;
		System.out.println("query in getLabelByEntity is: " + query);
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
		Entity a = new Entity(Common.PERSONLABEL, "黄晓明");
		Entity b = new Entity(Common.PERSONLABEL, "angelababy");

		// note that the relation point from a to b
		buildCypherSQL.InsertRelation(a, b, "老婆", null);

	}

}
