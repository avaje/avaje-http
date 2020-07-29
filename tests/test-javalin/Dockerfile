FROM openjdk:13-alpine
#FROM openjdk:11-slim

EXPOSE 7000
#VOLUME /var/log/java

WORKDIR /app
COPY target/lib /app/lib
COPY target/app.jar /app/app.jar

# For local docker build
#COPY application.yml /app/application.yml

ENV JAVA_OPTS=""
ENTRYPOINT java ${JAVA_OPTS} -jar app.jar


## openjdk class archive
#COPY target/class-archive.jsa /app/class-archive.jsa
#ENTRYPOINT java ${JAVA_OPTS} -Xshare:on -XX:SharedArchiveFile=class-archive.jsa -Xlog:class+load=warning -jar app.jar

## open9 shared classes
#FROM adoptopenjdk/openjdk11-openj9:nightly
#RUN mkdir /opt/shareclasses
#ENTRYPOINT java -Xshareclasses:cacheDir=/opt/shareclasses -jar app.jar
