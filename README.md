# How to install

## Add third party libs to in the local maven repo

In order, for the project to build, run this command in the project root directory :

```bash
mvn install:install-file -Dfile=./libs/osmonaut-1.0.2.4.jar -DgroupId=net.morbz -DartifactId=osmonaut -Dversion=1.0.2.4 -Dpackaging=jar
```

## Compile source code

This project use maven in order to compile source file :

```bash
mvn clean install
```

# How to use
 
## Download

```bash
mvn exec:java -Dexec.mainClass="com.mappy.data.batches.tomtom.download.TomtomDownloader" -Dexec.args="/tmp/tomtomfiles 2016_09 yourLogin yourPassword"

cd /tmp/naturalEarth
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/cultural/10m_cultural.zip
wget http://www.naturalearthdata.com/http//www.naturalearthdata.com/download/10m/physical/10m_physical.zip
unzip -o -j 10m_cultural.zip
unzip -o -j 10m_physical.zip
```
## Generate
```bash
mvn exec:java -Dexec.mainClass="com.mappy.data.batches.GenerateFullPbf" -Dexec.args="Belgique,Luxembourg /tmp/tomtomfiles /tmp/data Europe.osm.pbf 2"
mvn exec:java -Dexec.mainClass="com.mappy.data.batches.merge.MergeNaturalEarthTomtom" -Dexec.args="/tmp/naturalEarth /tmp/data"
```