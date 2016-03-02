/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class ConfigKeyName {
	//==========Neo4j
	public static final String DB_NEO4J_SERVER_IP_NAME="db.neo4j.server.ip";
	public static final String DB_NEO4J_SERVER_PORT_NAME="db.neo4j.server.port";
	public static final String DB_NEO4J_USER_NAME="db.neo4j.user";
	public static final String DB_NEO4J_PASSWD_NAME="db.neo4j.password";
	public static final String DB_NEO4J_DRIVERNAME_NAME="db.neo4j.drivername";
	
	//===========System.getProperty("user.dir") + "/"+"config/"+
	public static String ConfigFileName=System.getProperty("user.dir") + "/"+"config/"+"KG.property";

	public static void main(String args[])
	{
	}
	
}
