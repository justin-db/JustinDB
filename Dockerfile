FROM openjdk:8-jre

ADD run.sh /app/run.sh
RUN chmod +x /app/run.sh

ADD justin-http-client/target/scala-2.12/justin-http-client-assembly-0.1-642-gbcbb380-SNAPSHOT.jar /app/app.jar

EXPOSE 80 81 82 2551 2552 2553

ENV SERVICE_NAME justindb-cluster

ENTRYPOINT ["app/run.sh"]