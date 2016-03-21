/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.MR;
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

import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.Neo4jConfigBean;

import org.apache.hadoop.fs.Path;
public class ExtractorJob {
	public static String Type_Neo4j="Neo4j";
	public static String Type_Solr="Solr";

	@SuppressWarnings("deprecation")
	public static void main(String args[])
	{
		 try {
			  if(args.length!=3) 
			  {
				  System.err.println("input two param:inputtable,desttable,Type");
				  System.exit(0);
			  }
		      Configuration conf = HBaseConfiguration.create();
		      conf.addResource(new Path("conf/hbase-site.xml"));
		      String inputTableName = args[0].trim();
		      String destTableName=args[1].trim();
		      String type=args[2].trim();
		      Scan scan = new Scan();
		      scan.setCaching(100);
		      scan.setCacheBlocks(false);
		     // conf.set(TableInputFormat.SCAN, convertScanToString(scan));
		      conf.set(TableInputFormat.INPUT_TABLE, inputTableName);
		      conf.set("destTable", destTableName);
		      conf.set("type", type);
		      conf.set("label", Common.PERSONLABEL);
				if(type.contains("Neo4j"))
				{
					ConfigManager cfg = new ConfigManager();
				    conf.set("DriverName", cfg.getNeo4jDriverName());
				    conf.set("Ip", cfg.getNeo4jServerIp());
				    conf.set("Password", cfg.getNeo4jPasswd());
				    conf.setInt("Port", cfg.getNeo4jServerPort());
				    conf.set("User", cfg.getNeo4jUserName());
				}
				if(type.contains("Solr"))
				{
					
				}
		      Job job = new Job(conf);
		      job.setInputFormatClass(TableInputFormat.class);
		      job.setOutputFormatClass(TableOutputFormat.class);
		      job.setMapOutputValueClass(Text.class);
		      job.setMapOutputKeyClass(ImmutableBytesWritable.class);
		      job.setOutputKeyClass(ImmutableBytesWritable.class);
		      job.setOutputValueClass(Writable.class);
		      job.setOutputFormatClass(MultiTableOutputFormat.class);
		      job.setMapperClass(ExtractorMap.class);
		      job.setReducerClass(ExtractorReduce.class);
		      job.setJarByClass(ExtractorMap.class);
		      job.setJobName("ExtractorJob");
		      System.exit(job.waitForCompletion(true) ? 0 : 1);
		    } catch (Exception e) {
		    	e.printStackTrace();
		        System.out.println(e.toString());
		    }
	}

}
