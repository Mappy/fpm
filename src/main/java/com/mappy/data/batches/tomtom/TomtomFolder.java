package com.mappy.data.batches.tomtom;

import javax.inject.Inject;
import javax.inject.Named;

public class TomtomFolder {
    private final String inputFolder;
    private final String zone;

    @Inject
    public TomtomFolder(
            @Named("com.mappy.data.tomtom.input") String inputFolder,
            @Named("com.mappy.data.tomtom.zone") String zone) {
        this.inputFolder = inputFolder;
        this.zone = zone;
    }

    public String getFile(String name) {
        if (name.startsWith("2d")) {
            return inputFolder + zone + "_" + name;
        }
        return inputFolder + zone + "___________" + name;
    }
}