#!/bin/bash

# import the env file from local, will use some better way later...
source docker/local.env

# update the config using the ENV variables...

# Start the service
# java -cp target/WebController-0.1-jar-with-dependencies.jar  -Ddebug.conf=$EWEB_DEBUG_FILE com.emotibot.webController.WebController $EWEB_CU_ADDR $EWEB_RC_ADDR $EWEB_PORT
