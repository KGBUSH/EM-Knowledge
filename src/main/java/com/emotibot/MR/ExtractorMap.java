/*
 * Copyright (c) 2016 by Emotibot Corporation. All rights reserved.
 * EMOTIBOT CORPORATION CONFIDENTIAL AND TRADE SECRET
 *
 * Primary Owner: taoliu@emotibot.com.cn
 */
package com.emotibot.MR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.util.LineReader;

import com.emotibot.DB.RedisClient;
import com.emotibot.common.Common;
import com.emotibot.extractor.BaikeExtractor;
import com.emotibot.extractor.PageExtractInfo;
import com.emotibot.neo4jprocess.BuildCypherSQL;
import com.emotibot.util.Entity;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class ExtractorMap  extends TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
	public static String URL = "url";
	public static String HTMLBODY = "html";
	public static String WORDS="words";
	public static String type = "";
	public static String Seperator = "ACBDGFX";
	public static String Other = "other";
	public static String md5 = "urlkey";

	public static Map<String, String> URLLabelMap = null;
	public static Map<String, String> URLMD5LabelAllMap = null;
	public static Map<String, String> WordLabelMap =null;
	public static List<String> fileList = null;
    public static String NodeOrRelation="";
    
    public static String RedisIP="";
    public static int RedisPort=0;
    public static RedisClient redis=null;
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
		if (type.contains("Neo4j")) {
			NodeOrRelation=context.getConfiguration().get("NodeOrRelation");
		}
        RedisIP=context.getConfiguration().get("RedisIP");;
        RedisPort=context.getConfiguration().getInt("RedisPort", 0);;

        redis = new RedisClient(RedisIP,RedisPort);

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
		fileList.add("/domain/pet.txt");
		fileList.add("/domain/sports.txt");
		fileList.add("/domain/tourism.txt");
		fileList.add("/domain/economy.txt");
		fileList.add("/domain/medical_treatment.txt");
		fileList.add("/domain/job.txt");
	    fileList.add("/domain/music.txt");
	    WordLabelMap = new HashMap<>();
	    for (String f : fileList) {
	      getFileLine(f);
	    }
	    URLLabelMap = new HashMap<String,String>();
	    URLMD5LabelAllMap=new HashMap<String,String>();
        System.err.println("URLLabelMapSizeBB="+URLLabelMap.size()+"  URLMD5LabelAllMapSizeBB="+URLMD5LabelAllMap.size());
		URLLabelMap=getWordLabel("/domain/URLLabelMap.txt");
		URLMD5LabelAllMap=getWordLabel("/domain/URLMD5LabelAllMap.txt");
        System.err.println("URLLabelMapSizeBB="+URLLabelMap.size()+"  URLMD5LabelAllMapSizeBB="+URLMD5LabelAllMap.size());
	}

	@Override
	protected void map(ImmutableBytesWritable key, Result value, Context context)
			throws IOException, InterruptedException {
		try {
			if (value == null || value.raw() == null)
				return;
			String url = "";
			String html = "";
			String pmWord="";
			String label = "";

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
				if ((WORDS).equals(Bytes.toString(kv[i].getQualifier()))) {
					pmWord = Bytes.toString(kv[i].getValue());
					//System.err.println("SSS="+pmWord+"###");
					//System.err.println("SSS2="+pmWord.trim()+"###");
					pmWord=URLDecoder.decode(pmWord.trim()).trim();
					//System.err.println("SSS3="+URLDecoder.decode(pmWord).trim()+"###");
					//MyTrim
					//System.err.println("SSS4="+pmWord.trim()+"###");
					//System.err.println("SSS5="+MyTrim(pmWord)+"###");
					pmWord=MyTrim(pmWord);
				}

			}
			System.err.println("url.size=" + url.length() + " html.size=" + html.length());
			System.err.println("url=" + url);
			ImmutableBytesWritable outputKey = new ImmutableBytesWritable();
			ImmutableBytesWritable outputValue = new ImmutableBytesWritable();
			if ((url != null && url.trim().length() > 0) && (html != null && html.trim().length() > 0)) {
				if(url.indexOf("baike.baidu.com")==-1) return;
				url=url.trim();
				if (type.contains("Neo4j")) {
					BaikeExtractor baikeExtractor = new BaikeExtractor(html);
					PageExtractInfo pageExtractInfo = baikeExtractor.ProcessPage();
					String name = pageExtractInfo.getName();
					name=MyTrim(name);
					boolean flag = name.equals(pmWord);

					outputKey.set(Bytes.toBytes(getASCIISum(url, 3)));

					if(NodeOrRelation.equals("1")||NodeOrRelation.equals("3"))
					{
					/*if(NodeOrRelation.equals("1"))
					{
						if(name!=null&&name.trim().length()>0){
						if(WordLabelMap.containsKey(name)){label=WordLabelMap.get(name);}
						}
						if(pmWord!=null&&pmWord.trim().length()>0){
						if(WordLabelMap.containsKey(pmWord)){label=WordLabelMap.get(pmWord);}
						}
					}*/
					System.err.println("label0="+label);
					if(label==null||label.trim().length()==0||label.contains(Other)) {
						System.err.println("label1="+label);
						label=this.getLabel(url, pageExtractInfo.getTags());
					}
					System.err.println("label2="+label);
					System.err.println("LabelInfo:"+name+"###"+pmWord+"###"+label);
					System.err.println("LabelInfoData:"+pmWord+"###"+label);
					System.err.println("LabelInfoData:"+name+"###"+label);

					BuildCypherSQL bcy = new BuildCypherSQL();
					pageExtractInfo.addAttr(md5, DigestUtils.md5Hex(url));
					String query = bcy.InsertEntityNode(label, pageExtractInfo.getParamMd5(), pageExtractInfo.getAttr());
					System.err.println(NodeOrRelation+" queryMap=" + query);
					/////////TongyiciMap
					////////duoyici
					if(pageExtractInfo.getDuoyici().length()>0) System.err.println("Duoyici:"+pageExtractInfo.getDuoyici()+"###"+pageExtractInfo.getTags()+"###"+url);
                     //					System.err.println("Tongyici1:"+name+"###"+pmWord+"###"+url+"###"+pageExtractInfo.GetSynonym());
                    String tongyici="";
					if(pageExtractInfo.getTongyici().trim().length()>0)
                    {
						tongyici=pageExtractInfo.getTongyici().trim();
                    }
					//System.err.println("Tongyici1:"+name+"tiletags"+"###"+pmWord+"###"+url+"###"+pageExtractInfo.GetSynonym()+",,,"+tongyici);
					System.err.println("Tongyici1:"+name+"tiletags"+"###"+pmWord+"###"+url+"###"+tongyici);

					//////////
					Map<String,String> WordLink=pageExtractInfo.getWordLinkMap();
					if(WordLink!=null&&WordLink.size()>0)
					{
					  for(String word:WordLink.keySet())
					  {
						System.err.println("Tongyici2:"+word+"###"+WordLink.get(word)+"###");
					  }
					}
					System.err.println("Weka:"+pageExtractInfo.getTags()+"###"+label+"###"+name+"###"+pmWord+"###"+url);
					//getAttrValueStr
					System.err.println("WekaNew:"+pageExtractInfo.getTags()+"###"+label+"###"+name+"###"+pmWord+"###"+url+"###"+pageExtractInfo.getAttrValueStr()+"###"+pageExtractInfo.getFirstPara());

					/////////
					 if (query == null || query.trim().length() == 0) return;
					 String urlmd5=DigestUtils.md5Hex(url);
					 String parammd5=pageExtractInfo.getParamMd5();
					 boolean isexist=isExistHtml(urlmd5,parammd5);
					 System.err.println(url+" isexist="+isexist);
					 boolean isexitname=isExistName(name);
					 if(isexitname)
					 {
						 System.err.println("existname="+name+"  "+url);
						 return ;
					 }
					 if(!isexist)
					 {
					   outputValue.set(Bytes.toBytes(pageExtractInfo.getParamMd5()+"###"+query));
					   context.write(outputKey, outputValue);
					 }
					}
					if(NodeOrRelation.equals("2"))
					{
						HashMap<String,List<String>> attr_Values = pageExtractInfo.getAttr_Values();
						BuildCypherSQL bcy = new BuildCypherSQL();
						
						/*if(name!=null&&name.trim().length()>0){
						if(WordLabelMap.containsKey(name)){label=WordLabelMap.get(name);}
						}
						if(pmWord!=null&&pmWord.trim().length()>0){
						if(WordLabelMap.containsKey(pmWord)){label=WordLabelMap.get(pmWord);}
						}*/
					    if(label==null||label.trim().length()==0||label.contains(Other)) label=this.getLabel(url, pageExtractInfo.getTags());
						System.err.println("label="+label);

						if(attr_Values!=null&&attr_Values.size()>0)
						{
							for(String attr:attr_Values.keySet())
							{
								List<String> list = attr_Values.get(attr);
								for(String val:list)
								{
									Entity a = new Entity(label, name,"Name");
									String label2=Other;
                                    String urlval=pageExtractInfo.getWordLink(val).trim();
                                    String urlvalmd5=DigestUtils.md5Hex(urlval);
            						/*if(val!=null&&val.trim().length()>0){
            							if(WordLabelMap.containsKey(val)){label2=WordLabelMap.get(val);}
            						}
            						if(label2==null||label2.trim().length()==0||label2.contains(Other)){
            						if(URLLabelMap.containsKey(urlval))
                                    {
                                    	label2=URLLabelMap.get(urlval).trim();
                                    }
            						}*/
            						if(label2==null||label2.trim().length()==0||label2.contains(Other)){
            						if(URLMD5LabelAllMap.containsKey(urlvalmd5))
                                    {
                                    	label2=URLMD5LabelAllMap.get(urlvalmd5).trim();
                                    }
            						}
            						if(label2==null||label2.trim().length()==0||label2.contains(Other))
            						{
            							label2=Other;
            						}

                                    System.err.println("urlval="+urlval+"urlvalmd5="+urlvalmd5+"label2="+label2);
                                    System.err.println("URLLabelMapSize="+URLLabelMap.size()+"  URLMD5LabelAllMapSize="+URLMD5LabelAllMap.size());
                            		//URLLabelMap=getWordLabel("/domain/URLLabelMap.txt");
                            		//URLMD5LabelAllMap=getWordLabel("/domain/URLMD5LabelAllMap.txt");

									Entity b = new Entity(label2,val,"Name");
									if(name.trim().equals(val.trim())){
										System.err.println(name+"(equals)" +val);
                                        continue;
									}
					                String query=bcy.InsertRelation(a, b, attr, null);	
					                String query2="";
					                System.err.println(NodeOrRelation+" queryMap=" + query);
									Entity aa = new Entity(label, DigestUtils.md5Hex(url),md5);
									//Entity b = new Entity(label2,val,"Name");
                                    if(urlval!=null&&urlval.trim().length()>0)
                                    {
                                    	Entity bb = new Entity(label2,DigestUtils.md5Hex(urlval),md5);
                                    	query2=bcy.InsertRelation(aa, bb, attr, null);	
    					                System.err.println(NodeOrRelation+" queryMap2=" + query2);
                                    }
									if (query !=null && query.trim().length()>0){
										outputValue.set(Bytes.toBytes(query));
										context.write(outputKey, outputValue);
									}
									if (query2 !=null && query2.trim().length()>0){
										outputValue.set(Bytes.toBytes(query2));
										context.write(outputKey, outputValue);
									}

								}
							}
						}
					}
				}
				if (type.contains("Solr")) {
					BaikeExtractor baikeExtractor = new BaikeExtractor(html);
					PageExtractInfo pageInfo = baikeExtractor.ProcessPage();
					 String urlmd5=DigestUtils.md5Hex(url);
					 String parammd5=pageInfo.getParamMd5();
					 String name = pageInfo.getName();
					 name=MyTrim(name);
					 boolean isexist=isExistHtml(urlmd5,parammd5);
					 boolean isexitname=isExistName(name);
					 if(isexist||isexitname)
					 {
							System.err.println("isexist||isexitname");
							return;
					 } 
					StringBuffer buffer = new StringBuffer();
					buffer.append(pageInfo.getName()+""+pageInfo.getParamMd5()).append(Seperator);
					buffer.append(pageInfo.getName()).append(Seperator);
					buffer.append(pageInfo.getAttrStr()).append(Seperator);
					buffer.append(pageInfo.getValueStr()).append(Seperator);
					buffer.append(pageInfo.getAttrValueStr()).append(Seperator);
					buffer.append(pageInfo.toSolrString());
					outputKey.set(Bytes.toBytes(getASCIISum(url, 3)));
					outputValue.set(Bytes.toBytes(buffer.toString()));
					context.write(outputKey, outputValue);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("MapException=" + e.getMessage());
		}
		return;
	}
	
	public String getLabel(String url,String tag)
	{
		if(URLLabelMap.containsKey(url)) return URLLabelMap.get(url);
		if(tag==null||tag.trim().length()==0) return Other;
		return getLabelByTags(url,tag);
	}
    public  String MyTrim(String s){
    	String str = s.replace(String.valueOf((char) 160), " ").trim();
    	return str;
    }

	public String getASCIISum(String url, int n) {
		long sum = 0;
		url = url.toLowerCase();
		for (int index = 0; index < url.length(); index++) {
			sum = sum + Math.abs(url.charAt(index) - 'a');
		}
		return String.valueOf(sum % n);
	}

	public Map<String, String> getWordLabel(String fileName) {
		Map<String, String> result  = new HashMap<String, String>();
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
			System.err.println("BB"+fileName);
			while (in.readLine(line) > 0) {
				if(line==null) continue;
				lineStr = line.toString().trim();
				if(lineStr==null||lineStr.length()==0) continue;
				String[] arr = lineStr.split("###");
				System.err.println(lineStr + "ZZZZZZ " + arr.length);
				if(arr!=null&&arr.length>=2)
				{
					result.put(arr[0].trim(), arr[1].trim());
				}
			}
			dis.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

///////////////////////////////////////////
	  public String getLabelByTags(String url1,String tags)
	  {
	    if(tags==null||tags.trim().length()==0) return Other;
	    tags=tags.trim();
	          HttpURLConnection conn = null;
	          try{
	            String urlStr="http://192.168.1.81:7000/tag?t="+URLEncoder.encode(tags, "UTF-8")+"&&md5="+DigestUtils.md5Hex(url1);
	                System.err.println("urlStr tags="+tags+"   "+urlStr);
	              URL url = new URL(urlStr);
	              conn = (HttpURLConnection) url.openConnection();
	              conn.setDoOutput(true);
	              conn.setRequestMethod("GET");
	              //conn.setRequestProperty("accept-charset", "UTF-8");
	              int responseCode = conn.getResponseCode();
	              /*if (responseCode != HttpURLConnection.HTTP_OK) {
	                      throw new RuntimeException("Failed : HTTP error code : "
	                          + conn.getResponseCode());
	                  } else {*/
	                      InputStream stream = conn.getInputStream();
	                      byte[] data = readInputStream(stream);
	                      String content = new String(data,StandardCharsets.UTF_8);
	                      if(content == null || content.trim().length() == 0)
	                      {
	                        System.err.println("tags1="+tags+"  "+Other);
	                        return Other;
	                      }
	                      JSON jsonObject =  JSONSerializer.toJSON(content);
	                      JSONObject json = (JSONObject)jsonObject;
	                      String result = String.valueOf(json.getString("result"));
	                      if(result == null || result.trim().length() == 0)
	                      {
	                        System.err.println("tags2="+tags+"  "+Other);
	                        return Other;
	                      }
	                      System.err.println("result="+result);
	                      return result;
	                 // }
	          } catch (Exception e) {
	                    e.printStackTrace();
	          }finally{
	            if(conn != null){
	               conn.disconnect();
	            }
	          }
	    return Other;
	  }
	     private byte[] readInputStream(InputStream inStream) {
	         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	         byte[] buffer = new byte[1024];
	         int len = 0;
	         try {
	             while ((len = inStream.read(buffer)) != -1) {
	                 outStream.write(buffer, 0, len);
	             }
	             inStream.close();
	         } catch (Exception e) {
	             e.printStackTrace();
	         }
	         return outStream.toByteArray();
	     }
////////////////////////////////////////////

	   
	   public boolean isExistHtml(String urlmd5,String parammd5)
	   {
		   if(redis==null)
		   {
	            redis = new RedisClient(RedisIP,RedisPort);
		   }
		   return redis.existKey(urlmd5, parammd5);
	   }
	   public boolean isExistName(String name)
	   {
		   if(redis==null)
		   {
	            redis = new RedisClient(RedisIP,RedisPort);
		   }
		   return redis.existKey(name);
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
		        System.err.println(lineStr + "MMMM" + label);
		        WordLabelMap.put(lineStr, label);
		      }
		      dis.close();
		      in.close();
		    } catch (Exception e) {
		      e.printStackTrace();
		    }

		  }
	   public static void main(String[] args)
	   {
		   //String tags
		   System.err.println(DigestUtils.md5Hex("农学类专业"));
	   }

}