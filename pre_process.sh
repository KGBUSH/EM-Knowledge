#!/bin/bash

cd lib  

mvn install:install-file -Dfile=hanlp-1.2.8.jar -DgroupId=com.emotibot -DartifactId=hanlp -Dversion=1.2.8 -Dpackaging=jar



