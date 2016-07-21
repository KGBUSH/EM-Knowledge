#!/bin/bash
GetDataShellPath="Shell/"
CurrPath=`pwd`
DATE=$(date +%Y_%m_%d_%H_%M_%S)
ErrAllLog=$GetDataShellPath$DATE
cat /dev/null > $ErrAllLog

echo "GetDataShellPath="$GetDataShellPath
echo "CurrPath="$CurrPath
echo "ErrAllLog="$ErrAllLog

echo "Time="$DATE
echo "start"
echo "flushall" | /home/taoliu/redis/redis-3.0.0/src/redis-cli 
###############################################################################################

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 1 > tmp
cat tmp > all
id=`cat tmp | grep "Submitted" | grep "application" | awk -F " " '{print $7}'`
cd $GetDataShellPath
sh getData.sh $id
cat Node/error >> $ErrAllLog
cd $CurrPath
###############################################################################################
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t22 liu Neo4j 3 > tmp
cat tmp >> all
id=`cat tmp | grep "Submitted" | grep "application" | awk -F " " '{print $7}'`
cd $GetDataShellPath
sh getData.sh $id
cat Node/error >> $ErrAllLog
cd $CurrPath
###############################################################################################
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 2  > tmp
cat tmp >> all
id=`cat tmp | grep "Submitted" | grep "application" | awk -F " " '{print $7}'`
cd $GetDataShellPath
sh getData.sh $id
cat Node/error >> $ErrAllLog
cd $CurrPath
