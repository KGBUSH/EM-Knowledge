package com.emotibot.neo4jprocess;

import java.util.Map;

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;

public class BuildCypherSQLTest {
	public static void main(String args[]) {
		BuildCypherSQL bcy = new BuildCypherSQL();
		String query = bcy.FindEntityInfo(Common.PERSONLABEL, "姚明");
		System.out.println(query);

	}
}
