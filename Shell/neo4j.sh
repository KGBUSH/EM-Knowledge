#!/bin/bash
echo "start"
echo "flushall" | /home/taoliu/redis/redis-3.0.0/src/redis-cli 
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 1

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t22 liu Neo4j 3

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 2
