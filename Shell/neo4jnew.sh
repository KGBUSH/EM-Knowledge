#!/bin/bash
GetDataShellPath="Shell/"
CurrPath=`pwd`
DATE=$(date +%Y_%m_%d_%H_%M_%S)
ErrAllLog=$GetDataShellPath$DATE
cat /dev/null > $ErrAllLog
logfile="log2"

echo "GetDataShellPath="$GetDataShellPath
echo "CurrPath="$CurrPath
echo "ErrAllLog="$ErrAllLog

echo "Time="$DATE
echo "start"
echo "flushall" | /home/taoliu/redis/redis-3.0.0/src/redis-cli 
###############################################################################################

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 1 
id=`cat $logfile | grep "Submitted" | grep "application" | awk -F " " '{print $7}' | sed -n 1p`
cd $GetDataShellPath
sh getData.sh $id
cat Node/error >> $ErrAllLog
cd $CurrPath
###############################################################################################
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t22 liu Neo4j 3 
id=`cat logfile | grep "Submitted" | grep "application" | awk -F " " '{print $7}' | sed -n 2p`
cd $GetDataShellPath
sh getData.sh $id
cat Node/error >> $ErrAllLog
cd $CurrPath
###############################################################################################
/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 2  
id=`cat logfile | grep "Submitted" | grep "application" | awk -F " " '{print $7}' | sed -n 3p`
cd $GetDataShellPath
sh getData.sh $id
cat Node/error >> $ErrAllLog
cd $CurrPath
