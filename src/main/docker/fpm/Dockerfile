FROM openjdk:8-jre-alpine

ADD target/fpm-1.1-SNAPSHOT.jar target/

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote.port=9502", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-cp", "/target/*"]
