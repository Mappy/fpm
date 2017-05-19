package com.mappy.data.batches.utils;

import com.vividsolutions.jts.geom.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opengis.feature.simple.SimpleFeature;

import java.io.UnsupportedEncodingException;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
@AllArgsConstructor
@Slf4j
public class ShapefileFeature implements Feature {
    private final SimpleFeature feature;
    private final boolean forceUTF8;

    public Integer getInteger(String attr) {
        return (Integer) feature.getAttribute(attr);
    }

    public Double getDouble(String attr) {
        return (Double) feature.getAttribute(attr);
    }

    public Long getLong(String attr) {
        return (Long) feature.getAttribute(attr);
    }

    public String getString(String attr) {
        String value = defaultNull((String) feature.getAttribute(attr));
        return forceUTF8 && value != null ? toUTF8(value) : value;
    }

    public Point getPoint() {
        return (Point) feature.getDefaultGeometryProperty().getValue();
    }

    public MultiLineString getMultiLineString() {
        return (MultiLineString) feature.getDefaultGeometryProperty().getValue();
    }

    public Polygon getPolygon() {
        Geometry geometry = getGeometry();
        if (geometry instanceof Polygon) {
            return (Polygon) geometry;
        }
        else if (geometry instanceof MultiPolygon) {
            MultiPolygon multipolygon = (MultiPolygon) geometry;
            if (multipolygon.getNumGeometries() == 1) {
                return (Polygon) multipolygon.getGeometryN(0);
            }
        }
        throw new IllegalStateException("Cannot read polygon: " + geometry);
    }

    public MultiPolygon getMultiPolygon() {
        return (MultiPolygon) feature.getDefaultGeometryProperty().getValue();
    }

    public Geometry getGeometry() {
        return (Geometry) feature.getDefaultGeometryProperty().getValue();
    }

    private static String defaultNull(String attribute) {
        return isBlank(attribute) ? null : attribute;
    }

    private static String toUTF8(String name) {
        try {
            return new String(name.getBytes("ISO-8859-1"), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            log.error("Unable to convert value={}", name, e);
            return "";
        }
    }
}