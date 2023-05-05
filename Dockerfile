FROM ubuntu:bionic

RUN apt-get update && apt-get upgrade && apt-get -y install openjdk-11-jre \
    procps

COPY squirls-cli/target/squirls-cli-2.0.1.jar /opt/squirls/squirls-cli-2.0.1.jar
COPY docker/squirls /opt/bin/squirls
RUN chmod +x /opt/bin/squirls

ENV PATH="/opt/bin:${PATH}"
