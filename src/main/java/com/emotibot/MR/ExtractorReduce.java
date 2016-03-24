/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.MR;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringEscapeUtils;
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
    public static  String DriverName = "";
    public static  String Ip = "";
    public static String Password = "";
    public static int Port = 0;
    public static String User = "";
    public static String NodeOrRelation="";
	@Override
	public void setup(Context context) {
		type = context.getConfiguration().get("type");
		if (type.contains("Neo4j"))  NodeOrRelation=context.getConfiguration().get("NodeOrRelation");

		outputTableName = context.getConfiguration().get("destTable");
		puttable = new ImmutableBytesWritable(Bytes.toBytes(outputTableName));
		///////
		if (type.contains("Neo4j")) {
		 DriverName = context.getConfiguration().get("DriverName");
		 Ip = context.getConfiguration().get("Ip");
		 Password = context.getConfiguration().get("Password");
		 Port = context.getConfiguration().getInt("Port", 0);
		 User = context.getConfiguration().get("User");

		conn = new EmotibotNeo4jConnection(DriverName, Ip, Port, User, Password) ;

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
				System.err.println("typeReduce=" + type+"  ");
				if (type.contains("Neo4j")) {
					String query = value.toString();
					System.err.println("queryReduce=" + query);
					//query = StringEscapeUtils.escapeSql(query);
					//System.err.println("queryReduce2=" + query);
                    if(query==null||query.trim().length()==0)
                    {
        				System.err.println("query==null||query==0");
        				continue;
                    }
					if (conn != null) {
					}
					else
					{
						conn = new EmotibotNeo4jConnection(DriverName, Ip, Port, User, Password) ;
					}
					Neo4jResultBean bean ;//= conn.executeCypherSQL(query);
					boolean result=false;
                    if(NodeOrRelation.equals("1"))
                    {
                    	bean=conn.executeCypherSQL(query);
    					System.err.println("bean="+bean.toString());

                    }
                    else
                    {
                    	result=conn.updateQuery(query);
    					System.err.println("result="+result);
                    }
				}
				if (type.contains("Solr")) {
                	long t11 = System.currentTimeMillis();
                  String line=value.toString();
                  String[] arr = line.split(Seperator);
				  System.err.println("arr.length="+arr.length);

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
					System.err.println("solrDocnum="+solrDocnum);
                    if(solrDocnum%1000==0) {
                    	solrDocnum=solrDocnum%1000;
                    	long t1 = System.currentTimeMillis();
                    	solr.Commit();
                    	long t2=System.currentTimeMillis();
    					System.err.println("time="+(t2-t1));

                    }
                   }
               	long t22 = System.currentTimeMillis();
				System.err.println("time2="+(t22-t11));

				}
			}
			if(solrDocnum>0) solr.Commit();
		} catch (Exception e) {
			System.err.println("ReduceException="+e.getMessage());

		}
	}
	public static void main(String args[])
	{
		//String sql="国家/'地区'";  
        //System.out.println("防SQL注入:"+StringEscapeUtils.escapeSql(sql)); //防SQL注入  
		String attr="国家/地区''';:,.!$%";
		attr = attr.replaceAll("[\\pP‘’“”]", "");
		System.out.println("attr="+attr);
	}
}
