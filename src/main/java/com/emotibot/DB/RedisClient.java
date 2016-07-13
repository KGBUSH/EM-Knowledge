package com.emotibot.DB;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
public class RedisClient {
    private Jedis jedis;//非切片额客户端连接
    private JedisPool jedisPool;//非切片连接池
    private String ip="";
    private int port =0;
    public RedisClient(String ipStr,int portNum) 
    { 
        ip=ipStr;
        port=portNum;
        initialPool(); 
        jedis = jedisPool.getResource(); 
    } 
 
    /**
     * 初始化非切片池
     */
    private void initialPool() 
    { 
        // 池基本配置 
        JedisPoolConfig config = new JedisPoolConfig(); 
        config.setMaxIdle(20);
        config.setMaxIdle(5); 
        config.setMaxWaitMillis(300000); 
        config.setTestOnBorrow(false); 
        jedisPool = new JedisPool(config,ip,port);
        System.err.println(ip+"===>"+port);
    }
    
    public void Clear()
    {
    	jedis.flushDB();
    }
    
   public boolean existKey(String urlmd5,String parammd5)
   {
	   if(urlmd5==null||urlmd5.trim().length()==0)
	   {
		   if(parammd5==null||parammd5.trim().length()==0) return false;
	   }
	   boolean isUrl=jedis.exists(urlmd5);
	   boolean isParam = jedis.exists(parammd5);
	   if(isUrl||isParam) return true;
	   return false;
   }
   public boolean existKey(String name)
   {
	   if(name==null||name.trim().length()==0)
	   {
		    return false;
	   }
	   boolean isName=jedis.exists(name);
	  // jedis.set(name, "");
	   if(isName) return true;
	   return false;
   }
   public String getKey(String key)
   {
	   if(key==null||key.trim().length()==0)
	   {
		    return "";
	   }
	   String value=jedis.get(key);
	   if(value==null) value="";
	   return value;
   }
   public void setKey(String key,String value)
   {
	    jedis.set(key, value);
   }
   
   public void lpush(String key,String value)
   {
       jedis.lpush(key,value);  
   }
   public List<String> LGetList(String key)
   {
	   return jedis.lrange(key,0,-1);
   }
   
   public void test()
   {
	   String a="java framework";
	          jedis.lpush(a,"spring");  
	          System.out.println(jedis.exists(a));  
	         // System.out.println(jedis.ex);  

   }

   public static void main(String args[])
   {
	   RedisClient redis = new RedisClient("192.168.1.73",6379);
	  /* System.err.println(redis.existKey("1"));
	   System.err.println(redis.existKey("1"));
	   //redis.Clear();
	   System.err.println(redis.existKey("1"));
	   System.err.println(redis.existKey("1"));	 

	   System.err.println(redis.getKey("77"));
	   redis.setKey("1", "2");
	   System.err.println(redis.getKey("1"));
	   redis.setKey("1", "2222");
	   System.err.println(redis.getKey("1"));

	   redis.Clear();*/
	   redis.test();


   }
}
