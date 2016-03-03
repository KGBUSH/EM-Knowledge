package com.emotibot.neo4jprocess;

public class BuildCypherSQL {

	// private final EmotibotNeo4jConnection conn = connect();

	// 1. create class Entity
	// 2. create class Property
	// 3. interface for getting Entity from neo4j
	// 4. interface for getting a propert of a entity from neo4j

	// get Entity Query
	public String getEntity(String label, String name, String value) {
		String query = "match (e:" + label + ") where " + name + "=" + value + " return id(ee) as id, e as entity";
		return query;
	}

	// public boolean setEntity(String Label, Hashtable<String, Object> ht){
	// try{
	// Statement stmt = conn.getNeo4jConnnection().createStatement();
	// String prop = "";
	// ht.elements()
	// for()
	// String query = "match (e:" + entity + ") return id(ee) as id, e as
	// entity";
	// ResultSet rs = stmt.executeQuery(query);
	//
	// Entity ent = new Entity((int) rs.getObject("id"));
	// Map<String, Object> m = (Map<String, Object>) rs.getObject("entity");
	// for (String key : m.keySet()) {
	// System.out.println(key + " " + m.get(key));
	// ent.addProperty(key, m.get(key));
	// }
	// return ent;
	// } catch (Exception e){
	//
	// }
	// return true;
	// }

	// // build connection
	// private final EmotibotNeo4jConnection connect() {
	// try {
	// // Querying
	// final ConfigManager configManager = new ConfigManager();
	// EmotibotNeo4jConnection conn = new
	// EmotibotNeo4jConnection(configManager.getNeo4jDriverName(),
	// configManager.getNeo4jServerIp(), configManager.getNeo4jServerPort(),
	// configManager.getNeo4jUserName(), configManager.getNeo4jPasswd());
	//
	// return conn;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	// }

}
