#!/bin/bash

# import the env file from local, will use some better way later...
#source docker/local.env

# update the config using the ENV variables...
mkdir -p $KG_PRO_DIR
impfile="$KG_PRO_DIR/config/implication.property"
kgfile="$KG_PRO_DIR/config/KG.property"

#generat debug config properties

echo "age=$KG_IMP_AGE" > $impfile
echo "computeYear=$KG_IMP_YEAR" >> $impfile
echo "webserver.port=$KG_RS_KG_PORT" > $kgfile
echo "db.neo4j.server.ip=$KG_NEO4J_IP" >> $kgfile
echo "db.neo4j.server.port=$KG_NEO4J_PORT" >> $kgfile
echo "db.neo4j.user=$KG_NEO4J_USER" >> $kgfile
echo "db.neo4j.password=$KG_NEO4J_PASSWD" >> $kgfile
echo "db.neo4j.drivername=$KG_NEO4J_DRIVERNAME" >> $kgfile
echo "index.solr.server.ip=$KG_SOLR_IP" >> $kgfile
echo "index.solr.server.port=$KG_SOLR_PORT" >> $kgfile
echo "index.solr.server.solrname=$KG_SOLR_NAME" >> $kgfile
echo "tcp.server.ip=$KG_TCP_IP" >> $kgfile
echo "tcp.server.port=$KG_TCP_PORT" >> $kgfile
echo "intent.server.ip=$KG_INTENT_IP" >> $kgfile
echo "intent.server.port=$KG_INTENT_PORT" >> $kgfile

cp src/main/java/hanlp.properties target/classes/
cd sentiment
./NE_AhoCarosick & 
cd ..
# Start the service
# java -cp target/WebController-0.1-jar-with-dependencies.jar  -Ddebug.conf=$EWEB_DEBUG_FILE com.emotibot.webController.WebController $EWEB_CU_ADDR $EWEB_RC_ADDR $EWEB_PORT
java -cp target/KnowledgeGraph-0.0.1.jar -Ddebug.conf=config/emotibot_debug_conf.properties com.emotibot.WebService.WebServer $EWEB_RS_KG_PORT
