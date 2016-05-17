#!/bin/bash
REPO=docker-reg.emotibot.com.cn:55688
CONTAINER=emotibotweb
# FIXME: should use some tag other than latest
#TAG=20160504
TAG=latest
DOCKER_IMAGE=$REPO/$CONTAINER:$TAG

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BUILDROOT=$DIR/..

# Build docker
cmd="docker build --no-cache -t $DOCKER_IMAGE -f $DIR/Dockerfile $BUILDROOT"
echo $cmd
eval $cmd
