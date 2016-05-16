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

mvn install:install-file -Dfile=ab-4.0.5-SNAPSHOT.jar -DgroupId=com.emotibot -DartifactId=ab -Dversion=4.0.5-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=hanlp-1.2.8.jar -DgroupId=com.emotibot -DartifactId=hanlp -Dversion=1.2.8 -Dpackaging=jar
mvn install:install-file -Dfile=weka.jar -DgroupId=com.emotibot -DartifactId=weka -Dversion=3.8.0 -Dpackaging=jar
# Build docker
cmd="docker build --no-cache -t $DOCKER_IMAGE -f $DIR/Dockerfile $BUILDROOT"
echo $cmd
eval $cmd
