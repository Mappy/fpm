package com.mappy.fpm.batches.tomtom.download.json.model;

import lombok.Value;

import java.util.List;

@Value
public class Releases {

    private final List<Release> content;

    @Value
    public static class Release {
        private final String version;
        private final String location;
    }
}
