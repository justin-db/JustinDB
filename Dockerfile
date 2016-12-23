FROM openjdk:8-jre

ADD run.sh /app/run.sh

ADD target/scala-2.12/justin-db_2.12.1-0.1-532-g5bfc9fc-SNAPSHOT-multi-jvm-assembly.jar /app/app.jar

EXPOSE 80 81 82 2551 2552 2553

ENV SERVICE_NAME justindb-cluster

ENTRYPOINT ["app/run.sh"]