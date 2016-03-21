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

import com.emotibot.extractor.BaikeExtractor;
import com.emotibot.extractor.PageExtractInfo;

public class ExtractorMap extends Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Text> {
	public static String URL = "url";
	public static String HTMLBODY = "html";

	@Override
	public void setup(Context context) {
	}

	@Override
	protected void map(ImmutableBytesWritable key, Result value, Context context)
			throws IOException, InterruptedException {
		try {
			if (value == null || value.raw() == null)  return;
			String url = "";
			String html = "";
			KeyValue[] kv = value.raw();
			for (int i = 0; i < kv.length; i++) {
				System.err.println("kv[i].getQualifier()="+Bytes.toString(kv[i].getQualifier()));
				if ((URL).equals(Bytes.toString(kv[i].getQualifier()))) {
					url = Bytes.toString(kv[i].getValue());
					System.err.println("url="+url);
				}
				if ((HTMLBODY).equals(Bytes.toString(kv[i].getQualifier()))) {
					html = Bytes.toString(kv[i].getValue());
				}
			}
			System.err.println("url.size="+url.length()+" html.size="+html.length());
			if ((url != null && url.trim().length() > 0) && (html != null && html.trim().length() > 0)) {
				BaikeExtractor baikeExtractor = new BaikeExtractor(html);
				PageExtractInfo pageExtractInfo = baikeExtractor.ProcessPage();
				String info = pageExtractInfo.toString();
				System.err.println("info.size="+info.length());
				context.write(key, new Text(info));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
     return ;
	}
}