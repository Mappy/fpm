package com.mappy.fpm.batches.tomtom.download.json.model;

import lombok.Value;

import java.util.List;

@Value
public class Products {

    private final List<Product> content;

    @Value
    public static class Product {
        private final String name;
        private final String location;
    }
}
