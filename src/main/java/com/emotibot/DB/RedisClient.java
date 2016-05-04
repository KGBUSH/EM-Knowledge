package com.emotibot.DB;

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
    
    private void Clear()
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
	   jedis.sadd(urlmd5, "");
	   jedis.sadd(parammd5, "");
	   if(isUrl||isParam) return true;
	   return false;
   }
   public static void main(String args[])
   {
	   RedisClient redis = new RedisClient("192.168.1.73",6379);
	   System.err.println(redis.existKey("1", "2"));
	   System.err.println(redis.existKey("1", "2"));

   }
}
