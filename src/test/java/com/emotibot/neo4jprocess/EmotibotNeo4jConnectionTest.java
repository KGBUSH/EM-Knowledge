package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

import com.emotibot.config.ConfigManager;

public class EmotibotNeo4jConnectionTest {

	public static void main(String args[]) {

		try {
			// Querying
			final ConfigManager configManager = new ConfigManager();
			EmotibotNeo4jConnection conn = new EmotibotNeo4jConnection(configManager.getNeo4jDriverName(),
					configManager.getNeo4jServerIp(), configManager.getNeo4jServerPort(),
					configManager.getNeo4jUserName(), configManager.getNeo4jPasswd());
			Statement stmt = conn.getNeo4jConnnection().createStatement();
			ResultSet rs = stmt.executeQuery("MATCH (ee:Person) WHERE ee.name = \"Keanu Reeves\" RETURN ee;");

			Map<String, String> map = new HashMap<String, String>();
			while (rs.next()) {
				System.out.println("test");

				if (rs.next()) {
					Map<String, Object> e = (Map<String, Object>) rs.getObject("ee");
					// System.out.println("name: " + e.get("name") + ", from: "
					// + e.get("from"));
					for (String key : e.keySet()) {
						System.out.println(key + "  " + e.get(key));
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	//
	// @Test
	// public void test() throws Exception {
	// connect();
	// System.out.println("test");
	//
	// }

}
