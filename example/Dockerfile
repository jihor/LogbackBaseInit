FROM docstore.rgs.ru:5000/centos-java:latest
MAINTAINER "jihor"

# add jar
ADD example.jar /

# expose ports
EXPOSE 8080
#remote debugging port, shouldn't be used in production
#EXPOSE 5005

# setup startup command
CMD ["bash", "-c", "java -jar  \
 ${JAVA_OPTS} \
 -Dlogging.file.directory=logs \
 -Dlogging.file.enabled=true \
 -Dlogging.level=INFO \
 -Dlogging.splunk.enabled=true \
 -Dlogging.splunk.host=$LOGGING_SPLUNK_HOST \
 -Dlogging.splunk.indexes.businessevents=businessevents \
 -Dlogging.splunk.indexes.main=acme \
 -Dlogging.splunk.port=$LOGGING_SPLUNK_PORT \
 -Dlogging.splunk.token=$LOGGING_SPLUNK_TOKEN \
 /example.jar"]
