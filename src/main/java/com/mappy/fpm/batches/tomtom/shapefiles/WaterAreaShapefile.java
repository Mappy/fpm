package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LargePolygonSplitter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class WaterAreaShapefile extends TomtomShapefile {

    private final NameProvider nameProvider;

    @Inject
    public WaterAreaShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(folder.getFile("wa.shp"));
        this.nameProvider = nameProvider;
        if(new File(folder.getFile("wa.shp")).exists()) {
            this.nameProvider.loadFromFile("wxnm.dbf");
        }
    }

    @Override
    public String getOutputFileName() {
        return "wa";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        Integer type = feature.getInteger("TYP");
        Map<String, String> tags = newHashMap();
        if (!type.equals(1)) {
            String name = feature.getString("NAME");
            if (name != null) {
                tags.put("name", name);
            }
            tags.putAll(nameProvider.getAlternateNames(feature.getLong("ID")));
            if (type.equals(2)) {
                tags.put("water", "lake");
            }
        }
        tags.put("natural", "water");
        tags.put("ref:tomtom", String.valueOf(feature.getLong("ID")));
        for (Geometry geometry : LargePolygonSplitter.split(feature.getMultiPolygon(), 0.01)) {
            write(serializer, tags, geometry);
        }
    }

    private static void write(GeometrySerializer serializer, Map<String, String> tags, Geometry intersection) {
        if (intersection instanceof Polygon) {
            serializer.write((Polygon) intersection, tags);
        }
        else if (intersection instanceof MultiPolygon) {
            serializer.write((MultiPolygon) intersection, tags);
        }
        else {
            throw new RuntimeException("Wrong type");
        }
    }
}
