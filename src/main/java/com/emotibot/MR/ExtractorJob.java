/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.MR;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobPriority;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

import com.emotibot.DB.RedisClient;
import com.emotibot.common.Common;
import com.emotibot.config.ConfigManager;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.util.Tool;

import org.apache.hadoop.fs.Path;
//com.emotibot.MR.ExtractorJob
public class ExtractorJob {
  public static String Type_Neo4j="Neo4j";
  public static String Type_Solr="Solr";

  @SuppressWarnings("deprecation")
  public static void main(String args[])
  {
     try {
        if(args.length!=3&&args.length!=4) 
        {
          System.err.println("input two param:inputtable,desttable,Type,(=4,1 Node ,2 Relation)");
          System.exit(0);
        }
          Configuration conf = HBaseConfiguration.create();
          conf.addResource(new Path("conf/hbase-site.xml"));
          String inputTableFile = args[0].trim();
          String destTableName=args[1].trim();
          String type=args[2].trim();
         // Scan scan = new Scan();
         // scan.setCaching(100);
         // scan.setCacheBlocks(false);
         // conf.set(TableInputFormat.INPUT_TABLE, inputTableName);
          conf.set("destTable", destTableName);
          conf.set("type", type);
          conf.set("label", "");
          ConfigManager cfg = new ConfigManager();

        if(type.contains("Neo4j"))
        {
            conf.set("DriverName", cfg.getNeo4jDriverName());
            conf.set("Ip", cfg.getNeo4jServerIp());
            conf.set("Password", cfg.getNeo4jPasswd());
            conf.setInt("Port", cfg.getNeo4jServerPort());
            conf.set("User", cfg.getNeo4jUserName());
            conf.set("NodeOrRelation",args[3].trim());
            conf.set("RedisIP",cfg.getRedisIP());
            conf.setInt("RedisPort",cfg.getRedisPort());
        }
        if(type.contains("Solr"))
        {
          //String ip,int port,String solrName
          String ip = cfg.getIndexSolrServerIp();
          int port = cfg.getIndexSolrServerPort();
          String solrName = cfg.getIndexSolrServerSolrName();
            conf.set("ip", ip);
            conf.setInt("port", port);
            conf.set("solrName", solrName);
            conf.set("RedisIP",cfg.getRedisIP());
            conf.setInt("RedisPort",cfg.getRedisPort());

        }
        Vector<String> tables = Tool.getFileLines(inputTableFile);
       // for(String inputTableName:tables){
          //conf.set(TableInputFormat.INPUT_TABLE, inputTableName);
          Job job = new Job(conf);
          job.setPriority(JobPriority.HIGH);
          job.setInputFormatClass(TableInputFormat.class);
          job.setOutputFormatClass(TableOutputFormat.class);
          job.setMapOutputValueClass(Text.class);
          job.setMapOutputKeyClass(ImmutableBytesWritable.class);
          job.setOutputKeyClass(ImmutableBytesWritable.class);
          job.setOutputValueClass(Writable.class);
          job.setOutputFormatClass(MultiTableOutputFormat.class);
          job.setMapperClass(ExtractorMap.class);
          if(type.contains("Solr")){
        	  job.setReducerClass(ExtractorReduce.class);
          }
          job.setJarByClass(ExtractorMap.class);
          job.setJobName("ExtractorJob");
         // System.exit(job.waitForCompletion(true) ? 0 : 1);
          ////////
			List<Scan> scanList = new ArrayList<Scan>();
            for(String table:tables){
	            Scan scan = new Scan();  
	            scan.setCaching(5000);  
	            scan.setCacheBlocks(false);  
	            scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, table.getBytes());  
	            scanList.add(scan);  
            }
          ///////////
          TableMapReduceUtil.initTableMapperJob(scanList, ExtractorMap.class, ImmutableBytesWritable.class, ImmutableBytesWritable.class, job);
          //TableMapReduceUtil.initTableReducerJob(table, reducer, job);
          while (true) {
				if (job.waitForCompletion(true)) break;
				Thread.sleep(10000);
			}
          System.err.println("  "+"Finished!!!");
       // }
        } catch (Exception e) {
          e.printStackTrace();
            System.out.println(e.toString());
        }
    
    //new ExtractorMap().getFileLine("/domain/TV_series.txt");

  }

}