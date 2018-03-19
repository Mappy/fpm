package com.mappy.fpm.batches.splitter;

import lombok.Data;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

@Data
public class BoundingBox {

    private static final GeometryFactory gf = new GeometryFactory();
    private final double minLat, minLong, maxLat, maxLong;

    public static BoundingBox create(String input) {
        String[] split = input.split(",");
        double lat1 = Double.valueOf(split[1]);
        double lat2 = Double.valueOf(split[3]);
        double lng1 = Double.valueOf(split[0]);
        double lng2 = Double.valueOf(split[2]);
        return new BoundingBox(
                Math.min(lat1, lat2),
                Math.min(lng1, lng2),
                Math.max(lat1, lat2),
                Math.max(lng1, lng2));
    }

    public Envelope envelope() {
        return gf.createLineString(new Coordinate[] { new Coordinate(minLong, minLat), new Coordinate(maxLong, maxLat) }).getEnvelopeInternal();
    }
}