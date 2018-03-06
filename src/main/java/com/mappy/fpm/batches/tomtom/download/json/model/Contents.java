package com.mappy.fpm.batches.tomtom.download.json.model;

import lombok.Value;

import java.util.List;

@Value
public class Contents {

    private final List<Content> contents;

    @Value
    public static class Content {
        private final String name;
        private final String location;
    }
}
