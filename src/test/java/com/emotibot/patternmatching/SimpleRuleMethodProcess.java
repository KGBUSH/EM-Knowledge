package com.emotibot.patternmatching;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.jdbc.Neo4jConnection;

import com.emotibot.common.Common;
import com.emotibot.util.Neo4jResultBean;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */

public class SimpleRuleMethodProcess {

	public String SimpleMethod() {
		String strRs = "";

		/*
		 * 1. Get all the words from NLP
		 */
		String[] words = new String[] { "姚明", "身高" };

		/*
		 * 2. Classify the words into Entity and Property by looking up the
		 * dictionary
		 */
		HashMap <String, Integer> hm  = new HashMap <String, Integer>();
		boolean [] status = new boolean[words.length];
		
		HashSet ent = this.getEntityName();
		HashSet prop = this.getPropertyName();
		
		for (String s : words) {
			if(ent.contains(s)) {
				hm.put(s, 1);
			} else if (prop.contains(s)) {
				hm.put(s, 2);
			} else {
				hm.put(s, 0);
			}
		}

		/**
		 * 3. Set simple rules to generate the query and get the result from DB
		 */

		return strRs;
	}

	private HashSet getEntityName() {
		String query = "match (e:" + Common.PERSONLABEL + ") return e.\"姓名\" as property";
		Neo4jConnection conn = null;
		return this.getSetfromDB(conn, query);
	}

	private HashSet getPropertyName() {
		String query = "match (e:" + Common.PERSONLABEL + ") return keys(e) as property";
		Neo4jConnection conn = null;
		return this.getSetfromDB(conn, query);
	}

	public HashSet getSetfromDB(Neo4jConnection conn, String query) {
		HashSet<String> hs = new HashSet<String>();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				// System.out.println((String) rs.getObject("property"));
				hs.add(rs.getObject("property").toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return hs;
	}

}
