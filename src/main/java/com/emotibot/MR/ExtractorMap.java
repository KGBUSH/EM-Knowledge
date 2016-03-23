/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.MR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
import org.apache.hadoop.util.LineReader;

import com.emotibot.common.Common;
import com.emotibot.extractor.BaikeExtractor;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.neo4jprocess.BuildCypherSQL;

public class ExtractorMap extends Mapper<ImmutableBytesWritable, Result, ImmutableBytesWritable, Text> {
	public static String URL = "url";
	public static String HTMLBODY = "html";
	public static String type = "";
	public static String label = "";
	public static String Seperator = "ACBDGFX";

	public static HashMap<String, String> WordLabelMap = null;
	public static List<String> fileList = null;

	/*
	 * /domain/TV_series.txt /domain/anime.txt /domain/catchword.txt
	 * /domain/college.txt /domain/computer_game.txt /domain/cosmetics.txt
	 * /domain/delicacy.txt /domain/digital_product.txt /domain/figure.txt
	 * /domain/major.txt /domain/movie.txt /domain/novel.txt /domain/sports.txt
	 * /domain/sports_organization.txt /domain/tourism.txt
	 * /domain/varity_show.txt
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.hadoop.mapreduce.Mapper#setup(org.apache.hadoop.mapreduce.
	 * Mapper.Context)
	 */
	@Override
	public void setup(Context context) {
		type = context.getConfiguration().get("type");
		label = context.getConfiguration().get("label");
		fileList = new ArrayList<String>();
		fileList.add("/domain/TV_series.txt");
		fileList.add("/domain/anime.txt");
		fileList.add("/domain/catchword.txt");
		fileList.add("/domain/college.txt");
		fileList.add("/domain/computer_game.txt");
		fileList.add("/domain/cosmetics.txt");
		fileList.add("/domain/delicacy.txt");
		fileList.add("/domain/digital_product.txt");
		fileList.add("/domain/figure.txt");
		fileList.add("/domain/major.txt");
		fileList.add("/domain/movie.txt");
		fileList.add("/domain/novel.txt");
		fileList.add("/domain/sports.txt");
		fileList.add("/domain/sports_organization.txt");
		fileList.add("/domain/tourism.txt");
		fileList.add("/domain/varity_show.txt");
		WordLabelMap = new HashMap<>();
		for (String f : fileList) {
			getFileLine(f);
		}
	}

	@Override
	protected void map(ImmutableBytesWritable key, Result value, Context context)
			throws IOException, InterruptedException {
		try {
			if (value == null || value.raw() == null)
				return;
			String url = "";
			String html = "";
			KeyValue[] kv = value.raw();
			for (int i = 0; i < kv.length; i++) {
				System.err.println("kv[i].getQualifier()=" + Bytes.toString(kv[i].getQualifier()));
				if ((URL).equals(Bytes.toString(kv[i].getQualifier()))) {
					url = Bytes.toString(kv[i].getValue());
					System.err.println("url=" + url);
				}
				if ((HTMLBODY).equals(Bytes.toString(kv[i].getQualifier()))) {
					html = Bytes.toString(kv[i].getValue());
				}
			}
			System.err.println("url.size=" + url.length() + " html.size=" + html.length());
			if ((url != null && url.trim().length() > 0) && (html != null && html.trim().length() > 0)) {
				/*
				 * BaikeExtractor baikeExtractor = new BaikeExtractor(html);
				 * PageExtractInfo pageExtractInfo =
				 * baikeExtractor.ProcessPage(); String info =
				 * pageExtractInfo.toString();
				 * System.err.println("info.size="+info.length());
				 * context.write(key, new Text(info));
				 */
				if (type.contains("Neo4j")) {
					BaikeExtractor baikeExtractor = new BaikeExtractor(html);
					PageExtractInfo pageExtractInfo = baikeExtractor.ProcessPage();
					String name = pageExtractInfo.getName();
					if (name != null && !WordLabelMap.containsKey(name)) {
						System.err.println("name is not contain in WordLabelMap " + name+" "+url);
						return;
					}
					label = WordLabelMap.get(name);
					BuildCypherSQL bcy = new BuildCypherSQL();
					String query = bcy.InsertEntityNode(label, pageExtractInfo.getName(), pageExtractInfo.getAttr());
					System.err.println("queryMap=" + query);
					if (query == null || query.trim().length() == 0)
						return;
					context.write(key, new Text(query));
				}
				if (type.contains("Solr")) {
					BaikeExtractor baikeExtractor = new BaikeExtractor(html);
					PageExtractInfo pageInfo = baikeExtractor.ProcessPage();
					// doc.addField("id", pageInfo.getName());
					// doc.addField(Name, pageInfo.getName());
					// doc.addField(Attr, pageInfo.getAttrStr());
					// doc.addField(Value, pageInfo.getValueStr());
					// doc.addField(AttrValue, pageInfo.getAttrValueStr());
					// doc.addField(Info, pageInfo.toSolrString());
					StringBuffer buffer = new StringBuffer();
					buffer.append(pageInfo.getName()).append(Seperator);
					buffer.append(pageInfo.getName()).append(Seperator);
					buffer.append(pageInfo.getAttrStr()).append(Seperator);
					buffer.append(pageInfo.getValueStr()).append(Seperator);
					buffer.append(pageInfo.getAttrValueStr()).append(Seperator);
					buffer.append(pageInfo.toSolrString());
					context.write(key, new Text(buffer.toString()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("MapException=" + e.getMessage());
		}
		return;
	}

	public void getFileLine(String fileName) {
		try {
			if (fileName == null || fileName.trim().length() == 0) {
				System.err.println("fileName==null||fileName.trim().length()==0");
				System.exit(0);
			}
			Configuration conf = new Configuration();
			FileSystem hdfs = FileSystem.get(conf);
			Path inPath = new Path(fileName);
			FSDataInputStream dis = hdfs.open(inPath);
			LineReader in = new LineReader(dis, conf);
			Text line = new Text();
			String lineStr = "";
			String label = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
			while (in.readLine(line) > 0) {
				// result.add(line.toString());
				lineStr = line.toString().trim();
				System.err.println(lineStr + "  " + label);
				WordLabelMap.put(lineStr, label);
			}
			dis.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}