package com.emotibot.neo4jprocess;
import java.util.ArrayList; 
import java.util.Enumeration; 
import java.util.HashMap; 
import java.util.Hashtable; 
import java.util.Iterator;
import java.util.Map;
import java.util.Properties; 
import java.util.Vector; 
/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
public class Neo4jDBManager { 
static private Neo4jDBManager instance;//唯一数据库连接池管理实例类 
static private int clients;                 //客户连接数 
private Neo4jConnectionPool pool=new Neo4jConnectionPool();//连接池 

/** 
  * 实例化管理类 
  */ 
public Neo4jDBManager(Neo4jConfigBean neo4jConfigBean) { 
  // TODO Auto-generated constructor stub 
  this.init(neo4jConfigBean); 
} 
/** 
  * 得到唯一实例管理类 
  * @return 
  */ 
static synchronized public Neo4jDBManager getInstance(Neo4jConfigBean neo4jConfigBean) 
{ 
  if(instance==null) 
  { 
   instance=new Neo4jDBManager(neo4jConfigBean); 
  } 
  return instance; 
  
} 
/** 
  * 释放连接 
  * @param name 
  * @param con 
  */ 
public void freeConnection(EmotibotNeo4jConnection con) 
{ 
	//Neo4jConnectionPool pool=(Neo4jConnectionPool)pools.get(name);//根据关键名字得到连接池 
    if(pool!=null)  pool.freeConnection(con);//释放连接 
} 
/** 
  * 得到一个连接根据连接池的名字name 
  * @param name 
  * @return 
  */ 
public EmotibotNeo4jConnection getConnection() 
{ 
	EmotibotNeo4jConnection con=null; 
 // pool=(Neo4jConnectionPool)pools.get(name);//从名字中获取连接池 
  con=pool.getConnection();//从选定的连接池中获得连接 
  if(con!=null) 
  System.out.println("得到连接。。。"); 
  return con; 
} 
/** 
  * 得到一个连接，根据连接池的名字和等待时间 
  * @param name 
  * @param time 
  * @return 
  */ 
public EmotibotNeo4jConnection getConnection(long timeout) 
{ 
  EmotibotNeo4jConnection con=null; 
  //pool=(Neo4jConnectionPool)pools.get(name);//从名字中获取连接池 
  con=pool.getConnection(timeout);//从选定的连接池中获得连接 
  System.out.println("得到连接。。。"); 
  return con; 
} 
/** 
  * 释放所有连接 
  */ 
public synchronized void release() 
{ 
      if(pool!=null) pool.release(); 
} 
/** 
  * 创建连接池 
  * @param props 
  */ 
private void createPools(Neo4jConfigBean neo4jConfigBean) 
{ 
	pool=new Neo4jConnectionPool();
	pool.setDriver(neo4jConfigBean.getDriverName());
	pool.setIP(neo4jConfigBean.getIp());
	pool.setPassword(neo4jConfigBean.getPassword());
	pool.setPort(neo4jConfigBean.getPort());
	pool.setUser(neo4jConfigBean.getUser());

  System.out.println("maxConn="+pool.getmaxConn()+"  isUsed="+pool.getUsed()); 
  //pool.put(neo4jConfigBean.getDriverName(), Neo4jPool); 
} 
/** 
  * 初始化连接池的参数 
  */ 
private void init(Neo4jConfigBean neo4jConfigBean) 
{ 
	createPools(neo4jConfigBean) ;
    System.out.println("创建连接池完毕。。。"); 
} 
 
/** 
  * @param args 
  */ 
public static void main(String[] args) { 
  // TODO Auto-generated method stub 
} 
} 