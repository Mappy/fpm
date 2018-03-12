package com.mappy.fpm.batches.tomtom.dbf.poi;

import lombok.Getter;

@Getter
public enum FeatureType {
    MOUNTAIN_PASS("HP");

    private final String tomtomValue;

    FeatureType(String tomtomValue){
        this.tomtomValue = tomtomValue;
    }

    public String getValue(){
        return tomtomValue;
    }
}
