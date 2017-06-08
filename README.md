# How to install

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