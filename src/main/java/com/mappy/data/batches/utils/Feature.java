package com.mappy.data.batches.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public interface Feature {
    Integer getInteger(String attr);

    Double getDouble(String attr);

    Long getLong(String attr);

    String getString(String attr);

    Point getPoint();

    MultiLineString getMultiLineString();

    Polygon getPolygon();

    MultiPolygon getMultiPolygon();

    Geometry getGeometry();
}