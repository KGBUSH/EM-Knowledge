package com.emotibot.neo4jprocess;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.util.Neo4jResultBean;

public class EmotibotNeo4jConnectionTest {
	public static void main(String args[]) {
		try {
			// Querying
			final ConfigManager configManager = new ConfigManager();
			EmotibotNeo4jConnection conn = new EmotibotNeo4jConnection(configManager.getNeo4jDriverName(),
					configManager.getNeo4jServerIp(), configManager.getNeo4jServerPort(),
					configManager.getNeo4jUserName(), configManager.getNeo4jPasswd());

			BuildCypherSQL bcy = new BuildCypherSQL();
			String query = bcy.FindEntityInfo(Common.PERSONLABEL, "姚明");
			Neo4jResultBean bean = conn.executeCypherSQL(query);
			System.out.println(bean.getResult());

			query = bcy.FindEntityAttr(Common.PERSONLABEL, "姚明", "age");
			bean = conn.executeCypherSQL(query);
			System.out.println(bean.getResult());

			// System.out.println("test");

			// Statement stmt = conn.getNeo4jConnnection().createStatement();
			// String query = "match (e:Person{name:\"姚明\"}) return
			// e.firstParamInfo as property";
			// ResultSet rs = stmt.executeQuery(query);
			// while (rs.next()) {
			// System.out.println((String) rs.getObject("property"));
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
