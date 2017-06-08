package com.mappy.fpm.batches.tomtom;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.palominolabs.metrics.guice.MetricsInstrumentationModule;

import java.io.File;

public class Tomtom2OsmModule extends AbstractModule {
    private final MetricRegistry metrics = new MetricRegistry();
    private final String inputFolder;
    private final String output;
    private final String zone;
    private final String splitterFolder;

    public Tomtom2OsmModule(String inputFolder, String output, String splitterFolder, String zone) {
        this.inputFolder = inputFolder;
        this.output = output;
        this.splitterFolder = splitterFolder;
        this.zone = zone;
    }

    @Override
    protected void configure() {
        addMetrics(zone);
        bindConstant().annotatedWith(Names.named("com.mappy.data.tomtom.input")).to(inputFolder);
        bindConstant().annotatedWith(Names.named("com.mappy.data.splitter.output")).to(splitterFolder);
        bindConstant().annotatedWith(Names.named("com.mappy.data.tomtom.zone")).to(zone);
        bindConstant().annotatedWith(Names.named("com.mappy.data.serializer.output")).to(output + File.separator + zone + ".osm.pbf");
        bindConstant().annotatedWith(Names.named("com.mappy.data.serializer.username")).to("Tomtom");
    }

    private void addMetrics(String product) {
        bindConstant().annotatedWith(Names.named("com.mappy.env")).to("snap");
        bindConstant().annotatedWith(Names.named("com.mappy.product")).to(product);
        bindConstant().annotatedWith(Names.named("com.mappy.geoentity.version")).to("databatches");
        bindConstant().annotatedWith(Names.named("com.mappy.geoentity.influxdb.url")).to("snap-lek-002.mappy.priv");
        bind(MetricRegistry.class).toInstance(metrics);
        install(new MetricsInstrumentationModule(metrics));
    }
}
