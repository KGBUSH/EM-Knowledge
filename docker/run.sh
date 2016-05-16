#!/bin/bash
REPO=docker-reg.emotibot.com.cn:55688
CONTAINER=knowlegegraph
TAG=latest
DOCKER_IMAGE=$REPO/$CONTAINER:$TAG

# Get env from env file
source local.env

#docker run -it $DOCKER_IMAGE /bin/bash
docker rm -f -v $CONTAINER
cmd="docker run -d --name $CONTAINER \
 -e SA_PORT=$SA_PORT \
 -p 16416:$SA_PORT \
   $DOCKER_IMAGE \
"

# Debug only
# cmd="docker run -it --name $CONTAINER \
#  -v `pwd`/pipcache:/root/cache \
#  -e SA_PORT=$SA_PORT \
#  -p 16416:$SA_PORT \
#  --entrypoint /bin/bash \
#  $DOCKER_IMAGE /bin/bash"

echo $cmd
eval $cmd
