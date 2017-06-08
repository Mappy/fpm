package com.mappy.fpm.batches.merge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class PolygonsUtils {
    public static Iterable<Polygon> polygons(Geometry g) {
        return polygonsFrom(g).collect(Collectors.toList());
    }

    public static Iterable<Polygon> polygons(MultiPolygon g) {
        return polygonsFrom(g).collect(Collectors.toList());
    }

    public static Stream<Polygon> polygonsFrom(Geometry g) {
        if (g instanceof Polygon) {
            return Stream.of((Polygon) g);
        }
        else if (g instanceof MultiPolygon) {
            Builder<Polygon> builder = Stream.builder();
            for (int i = 0; i < g.getNumGeometries(); i++) {
                builder.add((Polygon) g.getGeometryN(i));
            }
            return builder.build();
        }
        return Stream.empty();
    }

    public static Stream<Polygon> polygonsFrom(List<Geometry> geoms) {
        return geoms.stream().flatMap(PolygonsUtils::polygonsFrom);
    }

    public static Geometry difference(Geometry polygon, List<Geometry> query) {
        return query.stream().reduce(polygon, Geometry::difference);
    }

}