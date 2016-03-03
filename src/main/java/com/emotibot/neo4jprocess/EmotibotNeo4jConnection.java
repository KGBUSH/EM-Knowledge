package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.Statement;
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

	public Neo4jResultBean executeCypherSQL(String query) {
		Neo4jResultBean bean = new Neo4jResultBean();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				bean.setResult(rs.getObject(Common.ResultObj).toString());
//				System.out.println((String) rs.getObject("property"));
			}
			
			bean.setStatus(true);
		} catch (Exception e) {
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

			Entity ent = new Entity((int) rs.getObject("id"));
			Map<String, Object> m = (Map<String, Object>) rs.getObject("entity");
			for (String key : m.keySet()) {
				System.out.println(key + "  " + m.get(key));
				ent.addProperty(key, m.get(key));
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
