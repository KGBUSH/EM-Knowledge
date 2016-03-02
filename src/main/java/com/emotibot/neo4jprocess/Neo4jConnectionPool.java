package com.emotibot.neo4jprocess;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
public class Neo4jConnectionPool {
	private EmotibotNeo4jConnection con = null;
	private int inUsed = 0; // 使用的连接数
	private ArrayList<EmotibotNeo4jConnection> freeConnections = new ArrayList<EmotibotNeo4jConnection>();// 容器，空闲连接
	private int maxConn = 50; // 最大连接
	private String password; // 密码
	private String driver; // 驱动
	private String user; // 用户名
	private String ip;
	private int port;

	public Neo4jConnectionPool() {
		EmotibotNeo4jConnection con = null;
		inUsed = 0; // 使用的连接数
		freeConnections = new ArrayList<EmotibotNeo4jConnection>();// 容器，空闲连接
		password = ""; // 密码
		driver = ""; // 驱动
		user = ""; // 用户名
		ip = "";
		port = 0;
		maxConn = 50;
	}

	/**
	 * 创建连接池
	 * 
	 * @param driver
	 * @param name
	 * @param URL
	 * @param user
	 * @param password
	 * @param maxConn
	 */
	public Neo4jConnectionPool(String driver, String ip, int port, String user, String password, int maxConn) {
		this.driver = driver;
		this.user = user;
		this.password = password;
		this.maxConn = maxConn;
		this.ip = ip;
		this.port = port;
	}

	/**
	 * 用完，释放连接
	 * 
	 * @param con
	 */
	public synchronized void freeConnection(EmotibotNeo4jConnection con) {
		this.freeConnections.add(con);// 添加到空闲连接的末尾
		this.inUsed--;
	}

	/**
	 * timeout 根据timeout得到连接
	 * 
	 * @param timeout
	 * @return
	 */
	public synchronized EmotibotNeo4jConnection getConnection(long timeout) {
		EmotibotNeo4jConnection con = null;
		if (this.freeConnections.size() > 0) {
			con = (EmotibotNeo4jConnection) this.freeConnections.get(0);
			if (con == null)
				con = getConnection(timeout); // 继续获得连接
		} else {
			con = newConnection(); // 新建连接
		}
		if (this.maxConn == 0 || this.maxConn < this.inUsed) {
			con = null;// 达到最大连接数，暂时不能获得连接了。
		}
		if (con != null) {
			this.inUsed++;
		}
		return con;
	}

	/**
	 * 
	 * 从连接池里得到连接
	 * 
	 * @return
	 */
	public synchronized EmotibotNeo4jConnection getConnection() {
		EmotibotNeo4jConnection con = null;
		if (this.freeConnections.size() > 0) {
			con = (EmotibotNeo4jConnection) this.freeConnections.get(0);
			this.freeConnections.remove(0);// 如果连接分配出去了，就从空闲连接里删除
			if (con == null)
				con = getConnection(); // 继续获得连接
		} else {
			con = newConnection(); // 新建连接
		}
		if (this.maxConn == 0 || this.maxConn < this.inUsed) {
			con = null;// 等待 超过最大连接时
		}
		if (con != null) {
			this.inUsed++;
			System.out.println("　的连接，现有" + inUsed + "个连接在使用!");
		}
		return con;
	}

	/**
	 * 释放全部连接
	 * 
	 */
	public synchronized void release() {
		Iterator<EmotibotNeo4jConnection> allConns = this.freeConnections.iterator();
		while (allConns.hasNext()) {
			EmotibotNeo4jConnection con = (EmotibotNeo4jConnection) allConns.next();
			try {
				if(!con.close()) con.close();
				con=null;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		this.freeConnections.clear();
	}

	/**
	 * 创建新连接
	 * 
	 * @return
	 */
	private EmotibotNeo4jConnection newConnection() {
		try {
			Class.forName(driver);
			con = new EmotibotNeo4jConnection(this.driver, this.ip, this.port, this.user, this.password);
		    //if(con==null) newConnection();
			if(con==null) {System.out.println("Create EmotibotNeo4jConnection failed!");}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}

	// private String password; // 密码
	// private String driver; // 驱动
	// private String user; // 用户名
	// private String ip;
	// private int port;
	public void setPassword(String passwd) {
		this.password = passwd;
	}

	public String getPassword() {
		return this.password;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getDriver() {
		return this.driver;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return this.user;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

	public String getIP() {
		return this.ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	// private int inUsed = 0; // 使用的连接数
	// private int maxConn=50; // 最大连接
	public int getUsed() {
		return this.inUsed;
	}

	public int getmaxConn() {
		return this.maxConn;
	}
	public String Info()
	{
		StringBuffer buffer= new StringBuffer();
		buffer.append("Neo4jConnectionPool Info:").append("\r\n");
		buffer.append("inUsed="+inUsed).append("\t").append("maxConn="+maxConn).append("\r\n");
		return buffer.toString();
	}
}
