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
	
	//============solr
	public static final String  INDEX_SOLR_SERVER_IP="index.solr.server.ip";
	public static final String  INDEX_SOLR_SERVER_PORT="index.solr.server.port";
	public static final String	INDEX_SOLR_SERVER_SOLRNAME="index.solr.server.solrname";
	//============webserver
	public static final String WEBSERVER_PORT="webserver.port";
	
	//============TCP
	public static final String TCP_SERVER_IP="tcp.server.ip";
	public static final String TCP_SERVER_PORT="tcp.server.port";
	
	//===========System.getProperty("user.dir") + "/"+"config/"+
	public static String ConfigFileName=System.getProperty("user.dir") + "/"+"config/"+"KG.property";
	public static String ImplicationConfigName=System.getProperty("user.dir") + "/"+"config/"+"implication.property";
    
	//=======Redis
	public static final String REDIS_IP="redis.ip";
	public static final String REDIS_PORT="redis.port";
	
	//=======Intent Server
	public static final String INTENT_SERVER_IP="intent.server.ip";
	public static final String INTENT_SERVER_PORT="intent.server.port";
	public static void main(String args[])
	{
	}
	
}
