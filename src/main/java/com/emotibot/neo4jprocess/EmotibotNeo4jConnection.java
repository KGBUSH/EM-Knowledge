package com.emotibot.neo4jprocess;

import java.util.Properties;
import org.neo4j.jdbc.Driver;
import org.neo4j.jdbc.Neo4jConnection;

public class EmotibotNeo4jConnection {
	private boolean isBusy;
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

			setNeo4jConnnection(new Driver().connect("jdbc:neo4j://" + ip + ":" + port + "/", properties));
			// Neo4jConnection.getConnection("jdbc:neo4j://192.168.1.81:7474/");
			conn = new Driver().connect("jdbc:neo4j://192.168.1.81:7474/", properties);// Neo4jConnection.getConnection("jdbc:neo4j://192.168.1.81:7474/");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public boolean isBusy() {
		return isBusy;
	}

	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}

	public Neo4jConnection getNeo4jConnnection() {
		return conn;
	}

	public void setNeo4jConnnection(Neo4jConnection conn) {
		this.conn = conn;
	}

	//

}
