#!/bin/bash
REPO=docker-reg.emotibot.com.cn:55688
CONTAINER=knowlegegraph
TAG=$(git rev-parse --short HEAD)
DOCKER_IMAGE=$REPO/$CONTAINER:$TAG

# Load the env file
source $1
if [ $? -ne 0 ]; then
  if [ "$#" -eq 0 ];then
    echo "Usage: $0 <envfile>"
    echo "e.g., $0 dev.env"
  else
    echo "Erorr, can't open envfile: $1"
  fi
  exit 1
else
  echo "# Using envfile: $1"
fi

#docker run -it $DOCKER_IMAGE /bin/bash
docker rm -f -v $CONTAINER
cmd="docker run -d --name $CONTAINER \
 -e KG_PRO_DIR=$KG_PRO_DIR \
 -e KG_RS_KG_PORT=$KG_RS_KG_PORT \
 -e KG_IMP_AGE=$KG_IMP_AGE \
 -e KG_IMP_YEAR=$KG_IMP_YEAR \
 -e KG_NEO4J_IP=$KG_NEO4J_IP \
 -e KG_NEO4J_PORT=$KG_NEO4J_PORT \
 -e KG_NEO4J_USER=$KG_NEO4J_USER \
 -e KG_NEO4J_PASSWD=$KG_NEO4J_PASSWD \
 -e KG_NEO4J_DRIVERNAME=$KG_NEO4J_DRIVERNAME \
 -e KG_SOLR_IP=$KG_SOLR_IP \
 -e KG_SOLR_PORT=$KG_SOLR_PORT \
 -e KG_SOLR_NAME=$KG_SOLR_NAME \
 -e KG_TCP_IP=$KG_TCP_IP \
 -e KG_TCP_PORT=$KG_TCP_PORT \
 -v /etc/localtime:/etc/localtime \
 -v /home/deployer/debug_logs:/tmp/ \
 -p $KG_RS_KG_PORT:$KG_RS_KG_PORT \
 -p $KG_TCP_PORT:$KG_TCP_PORT \
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
