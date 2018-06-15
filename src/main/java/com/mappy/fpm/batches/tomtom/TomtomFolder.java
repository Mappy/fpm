package com.mappy.fpm.batches.tomtom;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TomtomFolder {
    private final String inputFolder;
    private final String zone;

    @Inject
    public TomtomFolder(
            @Named("com.mappy.fpm.tomtom.input") String inputFolder,
            @Named("com.mappy.fpm.tomtom.zone") String zone) {
        this.inputFolder = inputFolder;
        this.zone = zone;
    }

    public String getFile(String name) {
        if (name.startsWith("2d")) {
            return inputFolder + zone + "_" + name;
        }
        return inputFolder + zone + "___________" + name;
    }

    public List<String> getSMFiles() {
        File file = new File(inputFolder);

        return Stream.of(file.listFiles()).filter(f -> f.getName().startsWith(zone) && f.getName().endsWith("sm.shp")).map(f -> inputFolder + f.getName()).collect(toList());
    }
}