/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.config;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class ConfigManager implements ConfigInterface{
	final Properties properties;
	
	
    public ConfigManager()
    {
        properties = new Properties();
    	System.out.println(ConfigKeyName.ConfigFileName);
        try {
            properties.load(new FileInputStream(ConfigKeyName.ConfigFileName));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            String str = sw.toString();
            System.out.println(str);
        }
        System.out.println("getNeo4jServerIp="+getNeo4jServerIp());
        System.out.println("getNeo4jServerPort="+getNeo4jServerPort());
        System.out.println("getNeo4jUserName="+getNeo4jUserName());
        System.out.println("getNeo4jPasswd="+getNeo4jPasswd());
        System.out.println("getNeo4jDriverName="+getNeo4jDriverName());

        System.out.println("getIndexSolrServerIp="+getIndexSolrServerIp());
        System.out.println("getIndexSolrServerPort="+getIndexSolrServerPort());
        System.out.println("getIndexSolrServerSolrName="+getIndexSolrServerSolrName());
        System.out.println("WebserverPort="+getWebServerPort());


    }

    
    @Override
    public String getTCPServerIp() {
    	// TODO Auto-generated method stub
    	if(properties!=null&&properties.containsKey(ConfigKeyName.TCP_SERVER_IP))
    		return properties.getProperty(ConfigKeyName.TCP_SERVER_IP);
    	return null;
    }
    
    @Override
    public int getTCPServerPort() {
    	// TODO Auto-generated method stub
    	try{
    		if(properties!=null&&properties.containsKey(ConfigKeyName.TCP_SERVER_PORT))
    			return Integer.valueOf(properties.getProperty(ConfigKeyName.TCP_SERVER_PORT));
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return 0;
    }

	@Override
	public String getNeo4jServerIp() {
		// TODO Auto-generated method stub
		if(properties!=null&&properties.containsKey(ConfigKeyName.DB_NEO4J_SERVER_IP_NAME))
			return properties.getProperty(ConfigKeyName.DB_NEO4J_SERVER_IP_NAME);
		return null;
	}

	@Override
	public int getNeo4jServerPort() {
		// TODO Auto-generated method stub
		try{
		if(properties!=null&&properties.containsKey(ConfigKeyName.DB_NEO4J_SERVER_PORT_NAME))
			return Integer.valueOf(properties.getProperty(ConfigKeyName.DB_NEO4J_SERVER_PORT_NAME));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getNeo4jUserName() {
		if(properties!=null&&properties.containsKey(ConfigKeyName.DB_NEO4J_USER_NAME))
			return properties.getProperty(ConfigKeyName.DB_NEO4J_USER_NAME);
		return null;
	}

	@Override
	public String getNeo4jPasswd() {
		// TODO Auto-generated method stub
		if(properties!=null&&properties.containsKey(ConfigKeyName.DB_NEO4J_PASSWD_NAME))
			return properties.getProperty(ConfigKeyName.DB_NEO4J_PASSWD_NAME);
		return null;
	}

	@Override
	public String getNeo4jDriverName() {
		// TODO Auto-generated method stub
		if(properties!=null&&properties.containsKey(ConfigKeyName.DB_NEO4J_DRIVERNAME_NAME))
			return properties.getProperty(ConfigKeyName.DB_NEO4J_DRIVERNAME_NAME);
		return null;
	}
 public static void main(String args[])
 {
	 ConfigManager ConfigManagerObj = new ConfigManager();
 }


@Override
public String getIndexSolrServerIp() {
	// TODO Auto-generated method stub
	if(properties!=null&&properties.containsKey(ConfigKeyName.INDEX_SOLR_SERVER_IP))
		return properties.getProperty(ConfigKeyName.INDEX_SOLR_SERVER_IP);
	return null;
}


@Override
public int getIndexSolrServerPort() {
	// TODO Auto-generated method stub
	try{
	if(properties!=null&&properties.containsKey(ConfigKeyName.INDEX_SOLR_SERVER_PORT))
		return Integer.valueOf(properties.getProperty(ConfigKeyName.INDEX_SOLR_SERVER_PORT));
	}catch(Exception e)
	{
		e.printStackTrace();
	}
	return 0;
}


@Override
public String getIndexSolrServerSolrName() {
	// TODO Auto-generated method stub
	if(properties!=null&&properties.containsKey(ConfigKeyName.INDEX_SOLR_SERVER_SOLRNAME))
		return properties.getProperty(ConfigKeyName.INDEX_SOLR_SERVER_SOLRNAME);
	return null;
}


@Override
public int getWebServerPort() {
	try{
	if(properties!=null&&properties.containsKey(ConfigKeyName.WEBSERVER_PORT))
		return Integer.valueOf(properties.getProperty(ConfigKeyName.WEBSERVER_PORT));
	}catch(Exception e)
	{
		e.printStackTrace();
	}

	return 0;
}


@Override
public String getRedisIP() {
	if(properties!=null&&properties.containsKey(ConfigKeyName.REDIS_IP))
		return properties.getProperty(ConfigKeyName.REDIS_IP);
	return null;
}


@Override
public int getRedisPort() {
	try{
	if(properties!=null&&properties.containsKey(ConfigKeyName.REDIS_PORT))
		return Integer.valueOf(properties.getProperty(ConfigKeyName.REDIS_PORT));
	}catch(Exception e)
	{
		e.printStackTrace();
	}

	return 0;
}
}
