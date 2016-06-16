package com.emotibot.neo4jprocess;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: quanzu@emotibot.com.cn
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;
import org.neo4j.jdbc.Neo4jStatement;

import com.emotibot.common.Common;
import com.emotibot.util.Entity;
import com.emotibot.util.Neo4jResultBean;

public class EmotibotNeo4jConnection {
	private Neo4jConnection conn;

	// build connection
	public EmotibotNeo4jConnection(Neo4jConfigBean neo4jConfigBean) {
		String driver = neo4jConfigBean.getDriverName();
		String ip = neo4jConfigBean.getIp();
		int port = neo4jConfigBean.getPort();
		String usr = neo4jConfigBean.getUser();
		String pwd = neo4jConfigBean.getPassword();
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
			List<String> ls = new ArrayList<>();
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
			String strRS = "";
			if (rs.next()) {
				strRS = rs.getObject(Common.ResultObj).toString();
			}
			return strRS;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public Neo4jResultBean executeCypherSQL(String query) {
		System.out.println("NEO4J: executeCypherSQL-->" + query);
		Neo4jResultBean bean = new Neo4jResultBean();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				if(rs.getObject(Common.ResultObj) == null){
					System.out.println("executeCypherSQL: rs.getresult was null");
					continue;
				}
				bean.setResult(rs.getObject(Common.ResultObj).toString());
				break;
			}
			
//			if (rs.next()) {
//				bean.setResult(rs.getObject(Common.ResultObj).toString());
//			}
			bean.setStatus(true);
		} catch (Exception e) {
			System.err.println("exception in NEO4J.executeCypherSQL, query="+query);
			e.printStackTrace();
			bean.setStatus(false);
			bean.setException(e.getMessage());
			return bean;
		}
		return bean;
	}
    // NOt support ny neo4j
	public boolean executeCypherSQLBatch(List<String> sqls) throws SQLException
	{
		Statement stmt =null;
		try
		{
			stmt = conn.createStatement();
			for(String sql:sqls)
			{
				if(sql!=null&&sql.trim().length()>0) stmt.addBatch(sql);
			}
			stmt.executeBatch();
		}catch(Exception e)
		{
			e.printStackTrace();
			if(stmt!=null) stmt.close();
			return false;
		}
		return true;
	}
	// get result Set
	public List<List<String>> getListSet(String query, List<String> propertySet) {
		List<List<String>> listSet = new ArrayList<>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				List<String> list = new ArrayList<>();
				for(String prop : propertySet){
					System.out.println("prop="+prop+", value="+rs.getObject(prop).toString());
					list.add(rs.getObject(prop).toString());
//					listSet.put(prop, rs.getObject(prop).toString());
				}
				listSet.add(list);
			}

			// System.out.println("object mpa is ="+mapEntity);
			return listSet;
		} catch (Exception e) {
			e.printStackTrace();
			return listSet;
		}
	}

	// get Entity only
	@SuppressWarnings("unchecked")
	public Map<String, Object> getEntityMap(String query) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			Map<String, Object> mapEntity = new HashMap<>();
			while (rs.next()) {
				if(rs.getObject(Common.ResultObj) == null){
					System.out.println("getEntityMap, rs is null");
					continue;
				}
				mapEntity = (Map<String, Object>) rs.getObject(Common.ResultObj);
				break;
			}
//			if (rs.next()) {
//				mapEntity = (Map<String, Object>) rs.getObject(Common.ResultObj);
//			}
			
			// System.out.println("object mpa is ="+mapEntity);
			return mapEntity;
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
			if (querys == null || querys.size() == 0)
				return true;
			StringBuffer buffer = new StringBuffer();
			for (String query : querys) {
				if (query != null && query.trim().length() > 0) {
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
