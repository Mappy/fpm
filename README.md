[![Build Status](https://travis-ci.org/Mappy/fpm.svg?branch=master)](https://travis-ci.org/Mappy/fpm)
[![Coverage Status](https://coveralls.io/repos/github/Mappy/fpm/badge.svg?branch=master)](https://coveralls.io/github/Mappy/fpm?branch=master)

[![GitHub release](https://img.shields.io/github/release/mappy/fpm.svg)]()

# How to install using a Docker environnement

```bash
cd src/main/docker/global_fpm
docker build -t mappy/fpm .
```

# How to install using a local environnement

## Add third party libs to in the local maven repo

In the project root directory :

```bash
mvn install:install-file -Dfile=./libs/osmonaut-1.0.2.4.jar -DgroupId=net.morbz -DartifactId=osmonaut -Dversion=1.0.2.4 -Dpackaging=jar
```

## Compile source code

This project use maven in order to compile source file :

```bash
mvn clean install

cp target/fpm-1.1-SNAPSHOT.jar src/main/docker/fpm/
cd src/main/docker/fpm
docker build -t mappy/fpm .
```

# How to use
 
## Download

```bash
docker run --rm -v /tmp/tomtomfiles:/workspace -p 9501:9501 -t mappy/fpm com.mappy.fpm.batches.tomtom.download.TomtomDownloader /workspace 2016_09  yourLogin yourPassword
```

```bash
cd /tmp/naturalEarth
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/10m_cultural.zip
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/physical/10m_physical.zip
unzip -o -j 10m_cultural.zip
unzip -o -j 10m_physical.zip
```

## Generate

```bash
docker run --rm -v /tmp/tomtomfiles:/input -v /tmp/data:/output -p 9501:9501 -t mappy/fpm com.mappy.fpm.batches.GenerateFullPbf "Belgique,Luxembourg" "/input" "/output" Europe.osm.pbf 2
docker run --rm -v /tmp/data:/workspace -v /tmp:/inputFolder -t mappy/fpm com.mappy.fpm.batches.merge.MergeNaturalEarthTomtom
```

To generate tolls data, a tolls.json file must be present in the /input directory.
