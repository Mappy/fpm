package com.mappy.fpm.batches.utils;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.*;

import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;

public class LargePolygonSplitter {
    private static final GeometryFactory gf = new GeometryFactory();

    public static List<Geometry> split(Geometry given, double maxArea) {
        return split(given, maxArea, (g) -> false);
    }

    public static List<Geometry> split(Geometry given, double maxArea, Predicate<Geometry> predicate) {
        List<Geometry> others = newArrayList();
        PriorityQueue<Geometry> queue = new PriorityQueue<>(comparing(Geometry::getArea).reversed());
        queue.add(given);

        while (queue.peek().getEnvelope().getArea() > maxArea) {
            Geometry current = queue.poll();
            Point centroid = current.getCentroid();
            Geometry bbox = current.getEnvelope();
            checkState(bbox.getCoordinates().length == 5);
            for (int i = 0; i < 4; i++) {
                Geometry intersection = current.intersection(box(centroid, bbox.getCoordinates()[i]));
                if (!intersection.isEmpty()) {
                    if (predicate.test(intersection)) {
                        others.add(intersection);
                    }
                    else {
                        queue.add(intersection);
                    }
                }
            }
        }

        return ImmutableList.<Geometry> builder().addAll(newArrayList(queue)).addAll(others).build();
    }

    private static Geometry box(Point centroid, Coordinate corner) {
        LineString lineString = gf.createLineString(new Coordinate[] { centroid.getCoordinate(), corner });
        return lineString.getEnvelope();
    }
}