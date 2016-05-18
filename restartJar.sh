#!/bin/bash
pid=`ps  -ef|grep  "KnowledgeGraph-0.0.1-SNAPSHOT.jar" | grep -v "grep"|awk '{print $2}'`;
echo "pid= ".$pid;
  if [[ ${pid} = '' ]];then
    echo " start check not exist";
  else
    echo " start echk exist".$pid;
    kill -9 $pid; 
  fi
pkill -9 NE_AhoCarosick
cd sentiment
nohup ./NE_AhoCarosick &
cd ..
nohup java -cp KnowledgeGraph-0.0.1.jar -Ddebug.conf=config/emotibot_debug_conf.properties com.emotibot.WebService.WebServer  &