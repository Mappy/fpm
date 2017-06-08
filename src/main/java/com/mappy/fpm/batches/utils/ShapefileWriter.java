package com.mappy.fpm.batches.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;

public class ShapefileWriter {

    public static <T> void write(File file,
            Consumer<SimpleFeatureTypeBuilder> fun1,
            BiConsumer<SimpleFeatureBuilder, T> fun2,
            List<T> geometries) {
        try {
            SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
            typeBuilder.setName("MyFeatureType");
            typeBuilder.setCRS(DefaultGeographicCRS.WGS84);
            fun1.accept(typeBuilder);
            SimpleFeatureType type = typeBuilder.buildFeatureType();
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
            AtomicInteger id = new AtomicInteger(0);
            List<SimpleFeature> geoms = geometries.stream().map(g -> {
                fun2.accept(featureBuilder, g);
                return featureBuilder.buildFeature(String.valueOf(id.getAndIncrement()));
            }).collect(toList());
            ShapefileDataStoreFactory datastoreFactory = new ShapefileDataStoreFactory();
            Map<String, Serializable> params = newHashMap();
            params.put("url", file.toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);
            ShapefileDataStore datastore = (ShapefileDataStore) datastoreFactory.createNewDataStore(params);
            datastore.createSchema(type);
            Transaction transaction = new DefaultTransaction("create");
            String typeName = datastore.getTypeNames()[0];
            SimpleFeatureSource featureSource = datastore.getFeatureSource(typeName);
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection collection = new ListFeatureCollection(type, geoms);
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(collection);
            transaction.commit();
            transaction.close();
            datastore.dispose();
        }
        catch (IOException e) {
            throw propagate(e);
        }
    }

    public static void write(File file, List<Geometry> geometries, Class<?> type) {
        write(file, typeBuilder -> typeBuilder.add("the_geom", type), SimpleFeatureBuilder::add, geometries);
    }
}