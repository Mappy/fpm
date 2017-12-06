FROM java:8

ADD fpm-1.1-SNAPSHOT.jar /target/

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote.port=9501", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-cp", "/target/*"]
