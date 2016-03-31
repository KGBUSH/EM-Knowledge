package com.emotibot.neo4jprocess;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

import com.emotibot.common.Common;
import com.emotibot.util.Entity;
import com.emotibot.util.Neo4jResultBean;

public class EmotibotNeo4jConnection {
	private Neo4jConnection conn;

	// build connection
	// (ip, port, usr, pwd)
	public EmotibotNeo4jConnection(String driver, String ip, int port, String usr, String pwd) {
		try {
			Class.forName(driver);

			// org.neo4j.jdbc.Driver
			// Connect
			Properties properties = new Properties();
			properties.put("user", usr);
			properties.put("password", pwd);

			// setNeo4jConnnection(new Driver().connect("jdbc:neo4j://" + ip +
			// ":" + port + "/", properties));
			// Neo4jConnection.getConnection("jdbc:neo4j://192.168.1.81:7474/");
			conn = new Driver().connect("jdbc:neo4j://" + ip + ":" + port + "/", properties);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			conn = null;
		}
	}

	// return list of string
	public List<String> getArrayListfromCollection(String query) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			List<String> ls = null;
			if (rs.next()) {
				ls = (List<String>) rs.getObject(Common.ResultObj);
			}
			return ls;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	// return a string
	public String getStringFromDB(String query) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			String strRS = null;
			if (rs.next()) {
				strRS = rs.getObject(Common.ResultObj).toString();
			}
			return strRS;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Neo4jResultBean executeCypherSQL(String query) {
		System.out.println("NEO4J: executeCypherSQL-->" + query);
		Neo4jResultBean bean = new Neo4jResultBean();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				bean.setResult(rs.getObject(Common.ResultObj).toString());
			}
			bean.setStatus(true);
		} catch (Exception e) {
			System.err.println("exception in NEO4J.executeCypherSQL");
			e.printStackTrace();
			bean.setStatus(false);
			bean.setException(e.getMessage());
			return bean;
		}
		return bean;
	}

	// get Entity only
	public Entity getEntity(String query) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			Entity ent = new Entity();
			Map<String, Object> m = (Map<String, Object>) rs.getObject("entity");
			for (String key : m.keySet()) {
				System.out.println(key + "  " + m.get(key));
				ent.addProperty(key, m.get(key).toString());
			}
			return ent;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// execute update queries
	public boolean updateQuery(String query) {
		try {
			conn.createStatement().executeQuery(query);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean updateQueryBatch(List<String> querys) {
		try {
			if(querys==null||querys.size()==0) return true;
			StringBuffer buffer = new StringBuffer();
			for(String query:querys)
			{
				if(query!=null&&query.trim().length()>0)
				{
					buffer.append(query).append("\r\n");
				}
			}
			String query = buffer.toString().trim();
			conn.createStatement().executeQuery(query);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * public boolean updateQueryBatch(List<String> querys) { try {
	 * if(querys==null||querys.size()==0) return true; Statement stmt =
	 * conn.createStatement(); for(String sql:querys) { stmt.addBatch(sql); }
	 * stmt.executeBatch(); return true; }catch(Exception e) {
	 * e.printStackTrace(); return false; } }
	 */

	public boolean close() {
		// close the connection
		try {
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Neo4jConnection getNeo4jConnnection() {
		return conn;
	}

	public void setNeo4jConnnection(Neo4jConnection conn) {
		this.conn = conn;
	}

	//

}
