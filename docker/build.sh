#!/bin/bash
REPO=docker-reg.emotibot.com.cn:55688
CONTAINER=knowlegegraph
# FIXME: should use some tag other than latest
#TAG=20160504
TAG=latest
DOCKER_IMAGE=$REPO/$CONTAINER:$TAG

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BUILDROOT=$DIR/..
cd $BUILDROOT

mkdir -p ./build
wget "http://docker-reg.emotibot.com.cn:50000/modules/knowledge_graph/ab-4.0.5-SNAPSHOT.jar" -O ./build/ab-4.0.5-SNAPSHOT.jar
wget "http://docker-reg.emotibot.com.cn:50000/modules/knowledge_graph/hanlp-1.2.8.jar" -O ./build/hanlp-1.2.8.jar
wget "http://docker-reg.emotibot.com.cn:50000/modules/knowledge_graph/weka.jar" -O ./build/weka.jar
wget "http://docker-reg.emotibot.com.cn:50000/libs/debug-logger-1.0-SNAPSHOT.jar" -O ./build/debug-logger-1.0-SNAPSHOT.jar
wget http://docker-reg.emotibot.com.cn:50000/modules/knowledge_graph/Hanlp.tar.gz -O ./build/Hanlp.tar.gz

##tar -zxvf Hanlp.tar.gz
##cp -r Hanlp/data .

# Build docker
cmd="docker build --no-cache -t $DOCKER_IMAGE -f $DIR/Dockerfile $BUILDROOT"
# cmd="docker build -t $DOCKER_IMAGE -f $DIR/Dockerfile $BUILDROOT"

echo $cmd
eval $cmd
