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
import org.apache.solr.common.SolrInputDocument;

import com.emotibot.config.ConfigManager;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.solr.SolrUtil;
import com.emotibot.util.Neo4jResultBean;

public class ExtractorReduce extends Reducer<ImmutableBytesWritable, Text, Writable, Put> {
	public static ImmutableBytesWritable puttable = new ImmutableBytesWritable();
	public static String outputTableName = "";
	public static String type = "";
	public static EmotibotNeo4jConnection conn = null;
    public static String Seperator="ACBDGFX";
	public static final String Name = "KG_Name";
	public static final String Attr = "KG_Attr";
	public static final String Value = "KG_Value";
	public static final String AttrValue = "KG_Attr_Value";
	public static final String Info = "KG_Info";
    public static SolrUtil solr = null;
	@Override
	public void setup(Context context) {
		type = context.getConfiguration().get("type");
		outputTableName = context.getConfiguration().get("destTable");
		puttable = new ImmutableBytesWritable(Bytes.toBytes(outputTableName));
		///////
		if (type.contains("Neo4j")) {
		String DriverName = context.getConfiguration().get("DriverName");
		String Ip = context.getConfiguration().get("Ip");
		String Password = context.getConfiguration().get("Password");
		int Port = context.getConfiguration().getInt("Port", 0);
		String User = context.getConfiguration().get("User");
		Neo4jConfigBean neo4jConfigBean = new Neo4jConfigBean();
		neo4jConfigBean.setDriverName(DriverName);
		neo4jConfigBean.setIp(Ip);
		neo4jConfigBean.setPassword(Password);
		neo4jConfigBean.setPort(Port);
		neo4jConfigBean.setUser(User);
		Neo4jDBManager neo4jDBManager = new Neo4jDBManager(neo4jConfigBean);
		conn = neo4jDBManager.getConnection();
		}
		else
		{
		solr = new SolrUtil();
		}
	}

	@Override
	protected void reduce(ImmutableBytesWritable key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		try {
			long solrDocnum=0;
			for (Text value : values) {
				if (type.contains("Neo4j")) {
					String query = value.toString();
					System.err.println("query=" + query);
					if (conn != null) {
						Neo4jResultBean bean = conn.executeCypherSQL(query);
						System.err.println(bean.toString());
					}
				}
				if (type.contains("Solr")) {
                  String line=value.toString();
                  String[] arr = line.split(Seperator);
                   if(arr!=null&&arr.length==6)
                   {
           			SolrInputDocument doc = new SolrInputDocument();
        			doc.addField("id", arr[0].trim());
        			doc.addField(Name, arr[1].trim());
        			doc.addField(Attr, arr[2].trim());
        			doc.addField(Value, arr[3].trim());
        			doc.addField(AttrValue, arr[4].trim());
        			doc.addField(Info, arr[5].trim());
                    solr.addDoc(doc);
                    solrDocnum++;
                    if(solrDocnum%100==0) {
                    	solrDocnum=solrDocnum%100;
                    	solr.Commit();
                    }
                   }

				}
			}
			if(solrDocnum>0) solr.Commit();
		} catch (Exception e) {
			System.err.println(e.getMessage());

		}
	}
}
