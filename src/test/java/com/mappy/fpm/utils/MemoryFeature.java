package com.mappy.fpm.utils;

import com.mappy.fpm.batches.utils.Feature;
import com.vividsolutions.jts.geom.*;

import java.util.Map;

public class MemoryFeature implements Feature {
    private static final GeometryFactory gf = new GeometryFactory();
    private final Geometry geom;
    private final Map<String, String> tags;

    private MemoryFeature(Geometry geom, Map<String, String> tags) {
        this.geom = geom;
        this.tags = tags;
    }

    public static MemoryFeature multilinestring(Map<String, String> tags, double lat1, double lon1, double lat2, double lon2) {
        return new MemoryFeature(gf.createMultiLineString(new LineString[] { gf.createLineString(new Coordinate[] {
                new Coordinate(lon1, lat1), new Coordinate(lon2, lat2)
        }) }), tags);
    }

    public static MemoryFeature     onlyTags(Map<String, String> tags) {
        return new MemoryFeature(null, tags);
    }

    @Override
    public Integer getInteger(String attr) {
        String value = getString(attr);
        return value != null ? Integer.valueOf(value) : null;
    }

    @Override
    public Double getDouble(String attr) {
        String value = getString(attr);
        return value != null ? Double.valueOf(value) : null;
    }

    @Override
    public Long getLong(String attr) {
        String value = getString(attr);
        return value != null ? Long.valueOf(value) : null;
    }

    @Override
    public String getString(String attr) {
        return tags.get(attr);
    }

    @Override
    public Point getPoint() {
        return (Point) geom;
    }

    @Override
    public MultiLineString getMultiLineString() {
        return (MultiLineString) geom;
    }

    @Override
    public Polygon getPolygon() {
        return (Polygon) geom;
    }

    @Override
    public MultiPolygon getMultiPolygon() {
        return (MultiPolygon) geom;
    }

    @Override
    public Geometry getGeometry() {
        return geom;
    }
}
