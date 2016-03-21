/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.MR;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.MultiTableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.util.Neo4jResultBean;

public  class ExtractorReduce extends
    Reducer<ImmutableBytesWritable, Text, Writable, Put> {
    public static ImmutableBytesWritable puttable = new ImmutableBytesWritable();
    public static String outputTableName = "";
    public static String type="";
    public static EmotibotNeo4jConnection conn=null;
  @Override
  public void setup(Context context) 
  {
	  type=context.getConfiguration().get("type");
	  outputTableName=context.getConfiguration().get("destTable");
      puttable = new ImmutableBytesWritable(Bytes.toBytes(outputTableName));  
      ///////
	     String DriverName= context.getConfiguration().get("DriverName");
	     String Ip= context.getConfiguration().get("Ip");
	     String Password= context.getConfiguration().get("Password");
	     int Port= context.getConfiguration().getInt("Port", 0);
	     String User= context.getConfiguration().get("User");	     
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(DriverName);
		neo4jConfigBean.setIp(Ip);
		neo4jConfigBean.setPassword(Password);
		neo4jConfigBean.setPort(Port);
		neo4jConfigBean.setUser(User);
		Neo4jDBManager neo4jDBManager = new Neo4jDBManager(neo4jConfigBean);
        conn=neo4jDBManager.getConnection();
  }


  @Override
  protected void reduce(ImmutableBytesWritable key,
      Iterable<Text> values, Context context) throws IOException,
      InterruptedException {
    String pageinfo = "";
    for (Text value : values) {
		if(type.contains("Neo4j"))
		{
			String query=value.toString();
			System.err.println("query="+query);
			if(conn!=null) 
			{
				Neo4jResultBean bean=conn.executeCypherSQL(query);
				System.err.println(bean.toString());
			}
		}
		if(type.contains("Solr"))
		{
			
		}
    }
  }
}

