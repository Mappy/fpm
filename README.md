[![Build Status](https://travis-ci.org/Mappy/fpm.svg?branch=master)](https://travis-ci.org/Mappy/fpm)
[![Coverage Status](https://coveralls.io/repos/github/Mappy/fpm/badge.svg?branch=master)](https://coveralls.io/github/Mappy/fpm?branch=master)

[![GitHub release](https://img.shields.io/github/release/mappy/fpm.svg)]()

# How to install

In the project root directory :

```bash
./build.sh
```

What this script does :
- Add a third party lib into the local maven repo : net.morbz.osmonaut
- Generate a jar, from source code, with maven
- Generate a docker image with previous jar

# How to use
 
## Download

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

## Generate

```bash
docker run --rm -v /tmp/tomtomfiles:/input -v /tmp/data:/output -p 9501:9501 -t mappy/fpm com.mappy.fpm.batches.GenerateFullPbf "Belgique,Luxembourg" "/input" "/output" Europe.osm.pbf 2
docker run --rm -v /tmp/data:/workspace -v /tmp:/inputFolder -t mappy/fpm com.mappy.fpm.batches.merge.MergeNaturalEarthTomtom
```

To generate tolls data, a tolls.json file must be present in the /input directory.