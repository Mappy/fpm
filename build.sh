#!/usr/bin/env bash

mvn install:install-file -Dfile=./libs/osmonaut-1.0.2.4.jar -DgroupId=net.morbz -DartifactId=osmonaut -Dversion=1.0.2.4 -Dpackaging=jar
mvn clean install

cp target/fpm-1.1-SNAPSHOT.jar src/main/docker/fpm/target

docker build -t mappy/fpm ./src/main/docker/fpm