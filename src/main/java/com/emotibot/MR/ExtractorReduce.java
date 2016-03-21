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

public  class ExtractorReduce extends
    Reducer<ImmutableBytesWritable, Text, Writable, Put> {
    public static ImmutableBytesWritable puttable = new ImmutableBytesWritable();
    public static String outputTableName = "";

  @Override
  public void setup(Context context) 
  {
	  outputTableName=context.getConfiguration().get("destTable");
      puttable = new ImmutableBytesWritable(Bytes.toBytes(outputTableName));  
  }


  @Override
  protected void reduce(ImmutableBytesWritable key,
      Iterable<Text> values, Context context) throws IOException,
      InterruptedException {
    String pageinfo = "";
    for (Text value : values) {
      pageinfo = value.toString();
     // int index = getASCIISum(value.toString(), puttablelist.size());
		System.err.println("outputTableName="+outputTableName);
		System.err.println("pageinfo="+pageinfo.length());

      Put put = new Put(DigestUtils.md5Hex(pageinfo).getBytes());
      //put.add("url".getBytes(), "url".getBytes(), value.toString().getBytes());
      put.add("url".getBytes(), "url".getBytes(), pageinfo.toString().getBytes());
      context.write(puttable, put);
    }
  }
}

