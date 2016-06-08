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
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.http.impl.client.SystemDefaultHttpClient;

import com.emotibot.config.ConfigManager;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.neo4jprocess.EmotibotNeo4jConnection;
import com.emotibot.neo4jprocess.Neo4jConfigBean;
import com.emotibot.neo4jprocess.Neo4jDBManager;
import com.emotibot.util.Neo4jResultBean;
import com.emotibot.util.Tool;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class ExtractorReduce extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
	public static ImmutableBytesWritable puttable = new ImmutableBytesWritable();
	public static String outputTableName = "";
	public static String type = "";
	public  EmotibotNeo4jConnection conn = null;
    public static String Seperator="ACBDGFX";
	public static final String Name = "KG_Name";
	public static final String Attr = "KG_Attr";
	public static final String Value = "KG_Value";
	public static final String AttrValue = "KG_Attr_Value";
	public static final String Info = "KG_Info";
   // public static SolrUtil solr = null;
	public HttpSolrClient solr =null;
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
	public static HashMap<String,String> DuplicateDetectionMap = new HashMap<>();
    public  static int BatchNum=100;
    public  static int BatchNumRelation=100;

	@Override
	public void setup(Context context) {
		type = context.getConfiguration().get("type");
		if (type.contains("Neo4j"))  NodeOrRelation=context.getConfiguration().get("NodeOrRelation");

		outputTableName = context.getConfiguration().get("destTable");
		puttable = new ImmutableBytesWritable(Bytes.toBytes(outputTableName));
		DuplicateDetectionMap = new HashMap<>();
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
			SystemDefaultHttpClient httpClient = new SystemDefaultHttpClient();
			solr = new HttpSolrClient("http://"+ip+":"+port+"/solr/"+solrName,httpClient);
		}
	}

	@Override
	protected void reduce(ImmutableBytesWritable folder,Iterable<ImmutableBytesWritable> values, Context context) 
			throws IOException, InterruptedException {
			long solrDocnum=0;
			List<String> list = new ArrayList<>();
			for (ImmutableBytesWritable value : values) {
		try {
				System.err.println("typeReduce=" + type+"  ");
				if (type.contains("Neo4j")) {
					String query=Bytes.toString(value.get());//value.toString();
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
    					String[] arrAll = query.split("###");
    					if(arrAll.length!=2) continue;
    					String md5sql=arrAll[0].trim();
    					query=arrAll[1].trim();
                        if(DuplicateDetectionMap.containsKey(md5sql)) continue;
                        DuplicateDetectionMap.put(md5sql, "");

            			if(query.contains("return")) query = query.substring(0, query.lastIndexOf("return"));
            			query=query.replaceAll("result", "result"+list.size());
                        list.add(query);
                        if(list.size()>(BatchNum-1))
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
                        if(list.size()>(BatchNumRelation-1))
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
					String line=Bytes.toString(value.get());//value.toString();
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
                    solr.add(doc);
                    solrDocnum++;
					System.err.println("solrDocnum="+solrDocnum);
                    if(solrDocnum%1000==0) {
                    	solrDocnum=solrDocnum%1000;
                    	long t1 = System.currentTimeMillis();
                    	solr.commit();
                    	long t2=System.currentTimeMillis();
    					System.err.println("time="+(t2-t1));

                    }
                   }
               	long t22 = System.currentTimeMillis();
				System.err.println("time2="+(t22-t11));

				}
			} catch (Exception e) {
				System.err.println("ReduceException="+e.getMessage());
			}

			}
			if(solrDocnum>0) {
				try{
				solr.commit();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
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
    				  if(k.contains("^M")) k=k.replaceAll("^M", "");
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
				index++;
			}
			buffer.append(bufferMatch.toString());
			buffer.append(bufferMerge.toString());
		}
		System.err.println("bufferMatch.toString()="+bufferMatch.toString());
		System.err.println("bufferMerge.toString()="+bufferMerge.toString());
		System.err.println("buffer.toString()="+buffer.toString());
		return buffer.toString();
	}
	public static void main(String args[])
	{
		String sql="dog days''";  
		sql=sql.replaceAll("'", "");

        //System.out.println("防SQL注入:"+StringEscapeUtils.escapeSql(sql)); //防SQL注入  
		//String attr="​abc return asd";
		//attr=attr.substring(0, attr.lastIndexOf("return"));
		//attr = attr.replaceAll("[\\pP‘’“”]", "");
		System.out.println(sql);
		/*String sql="match (p:Person {Name:\"黄晓明\"} ) match (q:Person {Name:\"angelababy\"} ) merge (p)-[r:老婆]->(q) ";
	    List<String> list = new ArrayList<String>();
	    Vector<String> sqls = Tool.getFileLines("sql");
	    for(int i=0;i<2;i++)
	    {
	    	String query=sqls.get(i);
			if(query.contains("return")) query = query.substring(0, query.lastIndexOf("return"));
		    list.add(query);

	    }
	    String s=getRelationsSql(list);
	    System.out.println(""+s);*/

	}
}
