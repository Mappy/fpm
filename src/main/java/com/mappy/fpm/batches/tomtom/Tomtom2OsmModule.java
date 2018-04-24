package com.mappy.fpm.batches.tomtom;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mappy.fpm.batches.tomtom.helpers.TollTagger;

import java.io.File;

public class Tomtom2OsmModule extends AbstractModule {
    private final String inputFolder;
    private final String output;
    private final String zone;
    private final String splitterFolder;
    private final TollTagger tollTagger;

    public Tomtom2OsmModule(String inputFolder, String output, String splitterFolder, TollTagger tollTagger, String zone) {
        this.inputFolder = inputFolder;
        this.output = output;
        this.splitterFolder = splitterFolder;
        this.zone = zone;
        this.tollTagger = tollTagger;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("com.mappy.fpm.tomtom.input")).to(inputFolder);
        bindConstant().annotatedWith(Names.named("com.mappy.fpm.splitter.output")).to(splitterFolder);
        bindConstant().annotatedWith(Names.named("com.mappy.fpm.tomtom.zone")).to(zone);
        bindConstant().annotatedWith(Names.named("com.mappy.fpm.serializer.output")).to(output + File.separator + zone);
        bindConstant().annotatedWith(Names.named("com.mappy.fpm.serializer.username")).to("Tomtom");
        bind(TollTagger.class).toInstance(tollTagger);
    }
}
