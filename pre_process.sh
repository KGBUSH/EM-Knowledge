#!/bin/bash

cd lib  

vn install:install-file -Dfile=ab-4.0.5-SNAPSHOT.jar -DgroupId=com.emotibot -DartifactId=ab -Dversion=4.0.5-SNAPSHOT -Dpackaging=jar

mvn install:install-file -Dfile=hanlp-1.2.8.jar -DgroupId=com.emotibot -DartifactId=hanlp -Dversion=1.2.8 -Dpackaging=jar




