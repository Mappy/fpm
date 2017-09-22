package com.mappy.fpm.batches.merge;

import com.google.inject.AbstractModule;

import static com.google.inject.name.Names.named;

public class MergeNaturalEarthTomtomModule extends AbstractModule {

    @Override
    protected void configure() {
        bindConstant().annotatedWith(named("com.mappy.fpm.geonames")).to("/inputFolder/geonames");
        bindConstant().annotatedWith(named("com.mappy.fpm.tomtom.data")).to("/inputFolder/tomtomfiles");
        bindConstant().annotatedWith(named("com.mappy.fpm.naturalearth.data")).to("/inputFolder/naturalearth");
        bindConstant().annotatedWith(named("com.mappy.fpm.serializer.output")).to("/workspace/merge.osm.pbf");
        bindConstant().annotatedWith(named("com.mappy.fpm.serializer.username")).to("boss");
    }
}