package com.emotibot.neo4jprocess;

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
import com.emotibot.util.Tool;

public class BuildCypherSQL implements CypherSQLParser {
	// get Entity Query
	public String getEntity(String label, String name, String value) {
		String query = "match (e:" + label + ") where " + name + "=" + value + " return id(ee) as id, e as entity";
		return query;
	}

	public String InsertEntityNodeByPageExtractInfo(PageExtractInfo pageInfo) {
		if (pageInfo == null)
			return Common.EMPTY;
		return InsertEntityNode(Common.PERSONLABEL, pageInfo.getName(), pageInfo.getAttr());
	}

	public String getPropNamebyEntityName(String label, String ent) {
		String query = "";
		if (Tool.isStrEmptyOrNull(ent) || Tool.isStrEmptyOrNull(label)) {
			// TBD
		} else {
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + ent + "\"}) return keys(e) as "
					+ Common.ResultObj;
		}
		System.out.println("query in getPropNamebyEntityName is: " + query);
		return query;
	}

	@Override
	public String InsertEntityNode(String Label, String name, Map<String, String> attr) {
		// TODO Auto-generated method stub
		// merge (n:Person{id:11}) set n.名字="姚明",n.position="C" return n
		String query = "";
		if (Tool.isStrEmptyOrNull(Label) || Tool.isStrEmptyOrNull(name) || (attr == null || attr.size() == 0)) {
		}
		{
			query = "merge (" + Common.ResultObj + ":Person{" + Common.KGNODE_NAMEATRR + ":\"" + name + "\"}) set ";
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
	public String InsertEntityEdge(String LabelA, String nameA, String relation, String LabelB, String nameB) {
		// TODO Auto-generated method stub
		return null;
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
		if (Tool.isStrEmptyOrNull(label) || Tool.isStrEmptyOrNull(name)) {
		}
		{
			query = "match (e:" + label + "{" + Common.KGNODE_NAMEATRR + ":\"" + name + "\"}) return e." + attr + " as "
					+ Common.ResultObj;
		}
		System.out.println("query in FindEnitityAttr is: " + query);
		return query;
	}

}
