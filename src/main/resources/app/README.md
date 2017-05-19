# DATA viewer
## OSM2VectorTiles
Install and Generate your own Vector Tiles:
* [OSM2VectorTiles](http://osm2vectortiles.org/docs/own-vector-tiles/)

## OSRM
Install and build your [OSRM](https://github.com/Project-OSRM/osrm-backend/tree/master/docker) backend:
```
git clone https://github.com/Project-OSRM/osrm-backend.git
cd osrm-backend
./docker/build-image.sh
docker run -i -p 5000:5000 -e "CXX=g++-5" -e "CC=gcc-5" -v `pwd`:/home/mapbox/osrm-backend -t mapbox/osrm:linux
cd osrm-backend
./build/osrm-extract [your.osm.pbf] -p profiles/car.lua
./build/osrm-contract [your.osrm]
./build/osrm-routed [your.osrm]
```

## Geoentity
* install http-server
```
npm install http-server -g
```
* start the viewer
```
cd databatches/src/main/resources/app/
http-server
```
* [enjoy](http://localhost:8081/)