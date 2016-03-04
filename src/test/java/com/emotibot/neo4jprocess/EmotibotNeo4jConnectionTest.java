package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
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
			String query = "MATCH (ee:Person{born:1964}) RETURN id(ee) as id, ee";
			ResultSet rs = stmt.executeQuery(query);
			// ResultSet rs = stmt.executeQuery("MATCH (ee:Person) WHERE ee.name
			// = \'Keanu Reeves\' RETURN ee;");

			while (rs.next()) {
				System.out.println("test1");
//				Map<String, Object> e = (Map<String, Object>) rs.getObject("id");
				Map<String, Object> e = (Map<String, Object>) rs.getObject("ee");
				System.out.println("id: " + rs.getObject("id") + ", name: " + e.get("name") + ", born: " + e.get("born"));
			}

			System.out.println("test2");
			Map<String, Object> t = (Map<String, Object>) rs.getObject("ee");
			for (String key : t.keySet()) {
				System.out.println(key + "  " + t.get(key));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
