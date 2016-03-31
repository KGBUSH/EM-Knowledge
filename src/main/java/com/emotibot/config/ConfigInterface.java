/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.config;

public interface ConfigInterface {

//	public static final String DB_NEO4J_SERVER_IP_NAME="db.neo4j.server.ip";
//	public static final String DB_NEO4J_SERVER_PORT_NAME="db.neo4j.server.port";
//	public static final String DB_NEO4J_USER_NAME="db.neo4j.user";
//	public static final String DB_NEO4J_PASSWD_NAME="db.neo4j.password";
//	public static final String DB_NEO4J_DRIVERNAME_NAME="db.neo4j.drivername";
	
	public String getNeo4jServerIp();
	public int getNeo4jServerPort();
	public String getNeo4jUserName();
	public String getNeo4jPasswd();
	public String getNeo4jDriverName();
	//public static final String  INDEX_SOLR_SERVER_IP="index.solr.server.ip";
	//public static final String  INDEX_SOLR_SERVER_PORT="index.solr.server.port";
	//public static final String	INDEX_SOLR_SERVER_SOLRNAME="index.solr.server.solrname";
    public String getIndexSolrServerIp();
    public int getIndexSolrServerPort();
    public String getIndexSolrServerSolrName();
    public int getWebServerPort();

}
