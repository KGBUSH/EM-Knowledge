#!/bin/bash

# import the env file from local, will use some better way later...
source docker/local.env

# update the config using the ENV variables...
mkdir -p $PARAM_PRO_DIR
impfile="$PARAM_PRO_DIR/config/implication.property"
kgfile="$PARAM_PRO_DIR/config/KG.property"

#generat debug config properties

echo "age=$PARAM_IMP_AGE" > $impfile
echo "computeYear=$PARAM_IMP_YEAR" >> $impfile
echo "webserver.port=$PARAM_RS_KG_PORT" > $kgfile
echo "db.neo4j.server.ip=$PARAM_NEO4J_IP" >> $kgfile
echo "db.neo4j.server.port=$PARAM_NEO4J_PORT" >> $kgfile
echo "db.neo4j.user=$PARAM_NEO4J_USER" >> $kgfile
echo "db.neo4j.password=$PARAM_NEO4J_PASSWD" >> $kgfile
echo "db.neo4j.drivername=$PARAM_NEO4J_DRIVERNAME" >> $kgfile
echo "index.solr.server.ip=$PARAM_SOLR_IP" >> $kgfile
echo "index.solr.server.port=$PARAM_SOLR_PORT" >> $kgfile
echo "index.solr.server.solrname=$PARAM_SOLR_NAME" >> $kgfile
echo "tcp.server.ip=$PARAM_TCP_IP" >> $kgfile
echo "tcp.server.port=$PARAM_TCP_PORT" >> $kgfile

# Start the service
# java -cp target/WebController-0.1-jar-with-dependencies.jar  -Ddebug.conf=$EWEB_DEBUG_FILE com.emotibot.webController.WebController $EWEB_CU_ADDR $EWEB_RC_ADDR $EWEB_PORT
java -cp KnowledgeGraph-0.0.1.jar -Ddebug.conf=$EWEB_DEBUG_FILE  com.emotibot.WebService.WebServer $EWEB_RS_KG_PORT