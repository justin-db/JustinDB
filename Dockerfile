FROM openjdk:8-jre

ADD run.sh /app/run.sh
RUN chmod +x /app/run.sh

ADD justin-db/client/http/target/scala-2.12/justin-db-client-http-assembly-0.1-538-g3ced791.jar /app/app.jar

EXPOSE 80 81 82 2551 2552 2553

ENV SERVICE_NAME justindb-cluster

ENTRYPOINT ["app/run.sh"]