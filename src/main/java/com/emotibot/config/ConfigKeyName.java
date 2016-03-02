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
	
public void g()
{
    Properties properties = new Properties();
	System.out.println(ConfigFileName);
    try {
        properties.load(new FileInputStream(ConfigFileName));
    } catch (Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        String str = sw.toString();
        System.out.println(str);
    }
    System.out.println(DB_NEO4J_DRIVERNAME_NAME+"="+properties.getProperty(DB_NEO4J_DRIVERNAME_NAME));

}
	public static void main(String args[])
	{
      new ConfigKeyName().g();
	}
	
}
