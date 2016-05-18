#!/bin/bash
pkill -9 KnowledgeGraph-0.0.1.jar
pkill -9 NE_AhoCarosick
cd sentiment
nohup ./NE_AhoCarosick &
cd ..
nohup java -cp KnowledgeGraph-0.0.1.jar -Ddebug.conf=config/emotibot_debug_conf.properties com.emotibot.WebService.WebServer  &
