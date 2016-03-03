package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

public class EmotibotNeo4jConnection {
	private boolean isBusy;
	// build connection
	// (ip, port, usr, pwd)
	public EmotibotNeo4jConnection(String ip, String port, String usr, String pwd) {
		try {
			Class.forName("org.neo4j.jdbc.Driver");
			
			// org.neo4j.jdbc.Driver
			// Connect
			Properties properties = new Properties();
			properties.put("user", "neo4j");
			properties.put("password", "123456");

			Neo4jConnection con = new Driver().connect("jdbc:neo4j://192.168.1.81:7474/", properties);
			// Neo4jConnection.getConnection("jdbc:neo4j://192.168.1.81:7474/");

			// Querying
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("MATCH (ee:Person) WHERE ee.name = \"Keanu Reeves\" RETURN ee;");
			// int index = 0;
			Map<String, String> map = new HashMap<String, String>();
			while (rs.next()) {
				//				index++;
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
	public boolean isBusy() {
		return isBusy;
	}
	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}

	//

}
