package com.mappy.fpm.batches.tomtom.download.json.model;

import lombok.Value;

import java.util.List;

@Value
public class Families {

    private final List<Family> content;

    @Value
    public static class Family {

        private final String abbreviation;
        private final String location;
    }
}
