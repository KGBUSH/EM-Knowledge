/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.MR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
import com.emotibot.util.Tool;

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
    ////
	public static String ip = "";
	public static int port = 0;
	public static String solrName ="";

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
			ip = context.getConfiguration().get("ip");
			port = context.getConfiguration().getInt("port", 0);
			solrName = context.getConfiguration().get("solrName");

		solr = new SolrUtil(ip,port,solrName);
		}
	}

	@Override
	protected void reduce(ImmutableBytesWritable key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		try {
			long solrDocnum=0;
			List<String> list = new ArrayList<>();
			for (Text value : values) {
				System.err.println("typeReduce=" + type+"  ");
				if (type.contains("Neo4j")) {
					String query = value.toString();
					//query = StringEscapeUtils.escapeSql(query);
					//System.err.println("queryReduce2=" + query);
                   // if(query.contains("return")) query=query.substring(0, query.lastIndexOf("return"));
					System.err.println("queryReduce=" + query);

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
                    if(NodeOrRelation.equals("1")||NodeOrRelation.equals("3"))
                    {
                        //bean=conn.executeCypherSQL(query);
                        //System.err.println("bean="+bean.toString());
            			if(query.contains("return")) query = query.substring(0, query.lastIndexOf("return"));
            			query=query.replaceAll("result", "result"+list.size());
                        list.add(query);
                        if(list.size()>100)
                        {
                        	result=conn.updateQueryBatch(list);
        					System.err.println("result="+result);
        					list.clear();
                        }

                    }
                    else
                    {
            			if(query.contains("return")) query = query.substring(0, query.lastIndexOf("return"));
                        list.add(query);
                        if(list.size()>100)
                        {
                          String queryBtch=getRelationsSql(list);
                    	  result=conn.updateQuery(queryBtch);
    					  System.err.println("result="+result);
    					  System.err.println("queryBtch="+queryBtch);
    					  list.clear();
                        }
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
			if(list.size()>0)
			{
                       if(NodeOrRelation.equals("1")||NodeOrRelation.equals("3"))
                       {
                        	boolean result=conn.updateQueryBatch(list);
        					System.err.println("result="+result);
        					list.clear();
                       }
                       else
                       {
                          String queryBtch=getRelationsSql(list);
                     	  boolean result=conn.updateQuery(queryBtch);
     					  System.err.println("result="+result);
    					  System.err.println("queryBtch="+queryBtch);

     					  list.clear();

                       }
			}
		} catch (Exception e) {
			System.err.println("ReduceException="+e.getMessage());

		}
	}
	
	public static String getRelationsSql(List<String> list)
	{
		StringBuffer bufferMatch = new StringBuffer();
		StringBuffer bufferMerge = new StringBuffer();
        StringBuffer buffer = new StringBuffer();
		if(list==null||list.size()==0) return "";
		else
		{
			int index=1;
			for(String sql:list)
			{
				sql=sql.replaceAll("p\\:", "p"+index+":");
				sql=sql.replaceAll("q\\:", "q"+index+":");
				sql=sql.replaceAll("r\\:", "r"+index+":");
				sql=sql.replaceAll("\\(p\\)", "(p"+index+")");
				sql=sql.replaceAll("\\(q\\)", "(q"+index+")");
                String[] arr = sql.split("match|merge");
                for(String k:arr)
                {
                	if(k!=null&&k.trim().length()>0)
                	{
        				k=k.trim();

    				System.err.println("k="+k);
    				  if(k.contains("[")&&k.contains("]"))
    				  {
    					bufferMerge.append("merge").append(k).append("\r\n");
    				  }
    				  else
    				  {
    					bufferMatch.append("match").append(k).append("\r\n");
    				  }
                	}
                }
				//System.err.println("sql="+sql);
				index++;
			}
			buffer.append(bufferMatch.toString());
			buffer.append(bufferMerge.toString());
		}
		return buffer.toString();
	}
	public static void main(String args[])
	{
		//String sql="国家/'地区'";  
        //System.out.println("防SQL注入:"+StringEscapeUtils.escapeSql(sql)); //防SQL注入  
		//String attr="​abc return asd";
		//attr=attr.substring(0, attr.lastIndexOf("return"));
		//attr = attr.replaceAll("[\\pP‘’“”]", "");
		//System.out.println("attr="+attr+"NN");
		String sql="match (p:Person {Name:\"黄晓明\"} ) match (q:Person {Name:\"angelababy\"} ) merge (p)-[r:老婆]->(q) ";
	    List<String> list = new ArrayList<String>();
	    Vector<String> sqls = Tool.getFileLines("sql");
	    for(int i=0;i<2;i++)
	    {
	    	String query=sqls.get(i);
			if(query.contains("return")) query = query.substring(0, query.lastIndexOf("return"));
		    list.add(query);

	    }
	    String s=getRelationsSql(list);
	    System.out.println(""+s);

	}
}
