package com.mappy.fpm.batches.utils;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Throwables.propagate;
import static org.opengis.filter.Filter.INCLUDE;

public class ShapefileIterator implements Iterator<Feature>, Closeable {
    private final FeatureIterator<SimpleFeature> features;
    private final ShapefileDataStore datastore;
    private final boolean forceUTF8;

    public ShapefileIterator(File file, boolean forceUTF8) {
        try {
            this.forceUTF8 = forceUTF8;
            datastore = new ShapefileDataStore(file.toURI().toURL());
            FeatureSource<SimpleFeatureType, SimpleFeature> source = datastore
                    .getFeatureSource(datastore.getTypeNames()[0]);
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(INCLUDE);
            features = collection.features();
        }
        catch (IOException e) {
            throw propagate(e);
        }
    }

    public ShapefileIterator(File file) {
        this(file, false);
    }

    public ShapefileIterator(Path path) {
        this(path.toFile(), false);
    }

    @Override
    public boolean hasNext() {
        return features.hasNext();
    }

    @Override
    public Feature next() {
        return new ShapefileFeature(features.next(), forceUTF8);
    }

    public Stream<Feature> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED), false);
    }

    @Override
    public void close() {
        features.close();
        datastore.dispose();
    }
}
