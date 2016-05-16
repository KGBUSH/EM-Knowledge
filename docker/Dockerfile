FROM maven:3.3.9-jdk-8

# Copy the source into the docker
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
ADD . /usr/src/app

# Rebuild the jar

RUN mkdir tmp
ADD ./build/*jar /tmp/
RUN cd /tmp;mvn install:install-file -Dfile=ab-4.0.5-SNAPSHOT.jar -DgroupId=com.emotibot -DartifactId=ab -Dversion=4.0.5-SNAPSHOT -Dpackaging=jar;mvn install:install-file -Dfile=hanlp-1.2.8.jar -DgroupId=com.emotibot -DartifactId=hanlp -Dversion=1.2.8 -Dpackaging=jar;mvn install:install-file -Dfile=weka.jar -DgroupId=com.emotibot -DartifactId=weka -Dversion=3.8.0 -Dpackaging=jar;mvn install:install-file -Dfile=debug-logger-1.0-SNAPSHOT.jar -DgroupId=com.emotibot -DartifactId=debug-logger -Dversion=1.0-SNAPSHOT -Dpackaging=jar;cd /usr/src/app;mvn install

# Entrypoint that reads the shell ENV variables as config flags
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]