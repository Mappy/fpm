# FPM

[![Build Status](https://travis-ci.org/Mappy/fpm.svg?branch=master)](https://travis-ci.org/Mappy/fpm)
[![Coverage Status](https://coveralls.io/repos/github/Mappy/fpm/badge.svg?branch=master)](https://coveralls.io/github/Mappy/fpm?branch=master)
[![GitHub release](https://img.shields.io/github/release/mappy/fpm.svg)]()

## How to install

### Prerequisites

- Java JDK 8
- Maven
- Docker (optional)
- Osmonaut (third party library)

The `Osmonaut` library can be installed from the project root directory with the following command:
```bash
mvn install:install-file -Dfile=./libs/osmonaut-1.0.2.4.jar -DgroupId=net.morbz -DartifactId=osmonaut -Dversion=1.0.2.4 -Dpackaging=jar 
```

### Build

In the project root directory:

```bash
./build.sh
```

What this script does:
- Add a third party library into the local maven repository: net.morbz.osmonaut
    ```bash
    mvn install:install-file -Dfile=./libs/osmonaut-1.0.2.4.jar -DgroupId=net.morbz -DartifactId=osmonaut -Dversion=1.0.2.4 -Dpackaging=jar 
    ```
- Generate a jar, from source code, with maven
    ```bash
    mvn clean install 
    ```
- Generate a docker image with the previous jar
    ```bash
    cp target/fpm-1.1-SNAPSHOT.jar src/main/docker/fpm/target
    docker build -t mappy/fpm ./src/main/docker/fpm
    ```


## How to use
 
### Download

```bash
docker run --rm -v /tmp/tomtomfiles:/workspace -p 9501:9501 -t mappy/fpm com.mappy.fpm.batches.tomtom.download.json.MapContentDownloader /workspace yourToken 2016.09
```

```bash
cd /tmp/naturalEarth
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/10m_cultural.zip
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/physical/10m_physical.zip
unzip -o -j 10m_cultural.zip
unzip -o -j 10m_physical.zip
```

### Generate

With Docker:
```bash
docker run --rm -v /tmp/tomtomfiles:/input -v /tmp/data:/output -p 9501:9501 -t mappy/fpm com.mappy.fpm.batches.GenerateFullPbf "Belgique,Luxembourg" "/input" "/output" Europe.osm.pbf 2
docker run --rm -v /tmp/data:/workspace -v /tmp:/inputFolder -t mappy/fpm com.mappy.fpm.batches.merge.MergeNaturalEarthTomtom
```

Or locally:
```bash
java -cp target/fpm-1.1-SNAPSHOT.jar com.mappy.fpm.batches.GenerateFullPbf "Belgique,Luxembourg" "/tmp/tomtomfiles" "/tmp/data" Europe.osm.pbf 2
```

To generate tolls data, a tolls.json file must be present in the /input directory.