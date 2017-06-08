package com.mappy.fpm.batches.naturalearth;

import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.ShapefileIterator;

import java.io.File;

public abstract class NaturalEarthShapefile {

    private final String filename;

    protected NaturalEarthShapefile(String filename) {
        this.filename = filename;
    }

    public void serialize(GeometrySerializer serializer) {
        try (ShapefileIterator iterator = new ShapefileIterator(new File(filename))) {
            while (iterator.hasNext()) {
                serialize(serializer, iterator.next());
            }
        }
    }

    public abstract void serialize(GeometrySerializer serializer, Feature feature);
}