#!/bin/bash
echo "flushall" | /home/taoliu/redis/redis-3.0.0/src/redis-cli 

echo "scrapy_baike_words_first" > t1
cat t1
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t1 liu Solr

echo "scrapy_baike_words_third" > t1
cat t1
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t1 liu Solr

echo "baike2_liutao_first" > t1  
cat t1
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t1 liu Solr 

echo "baike_liutao" > t1
cat t1
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t1 liu Solr

echo "baike2_liutao_second" > t1
cat t1
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t1 liu Solr  

echo "scrapy_baike_words_second" > t1
cat t1
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t1 liu Solr
 
