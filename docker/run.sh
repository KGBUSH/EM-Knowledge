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
 -e PARAM_RS_KG_PORT=$PARAM_RS_KG_PORT \
 -e PARAM_IMP_AGE=$PARAM_IMP_AGE \
 -e PARAM_IMP_YEAR=$PARAM_IMP_YEAR \
 -e PARAM_NEO4J_IP=$PARAM_NEO4J_IP \
 -e PARAM_NEO4J_PORT=$PARAM_NEO4J_PORT \
 -e PARAM_NEO4J_USER=$PARAM_NEO4J_USER \
 -e PARAM_NEO4J_PASSWD=$PARAM_NEO4J_PASSWD \
 -e PARAM_NEO4J_DRIVERNAME=$PARAM_NEO4J_DRIVERNAME \
 -e PARAM_SOLR_IP=$PARAM_SOLR_IP \
 -e PARAM_SOLR_PORT=$PARAM_SOLR_PORT \
 -e PARAM_SOLR_NAME=$PARAM_SOLR_NAME \
 -e PARAM_TCP_IP=$PARAM_TCP_IP \
 -e PARAM_TCP_PORT=$PARAM_TCP_PORT \
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
