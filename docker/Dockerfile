FROM maven:3.3.9-jdk-8

# Copy the source into the docker
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
ADD . /usr/src/app

# Rebuild the jar
RUN mvn install

# Entrypoint that reads the shell ENV variables as config flags
ENTRYPOINT ["/usr/src/app/entrypoint.sh"]
