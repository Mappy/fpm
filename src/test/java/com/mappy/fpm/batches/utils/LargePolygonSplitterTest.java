package com.mappy.fpm.batches.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LargePolygonSplitterTest {
    private static final GeometryFactory gf = new GeometryFactory();

    @Test
    public void should_not_split_small_polygon() throws Exception {
        Polygon polygon = polygon(
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(0.0, 0.0));

        List<Geometry> geoms = LargePolygonSplitter.split(polygon, 10);

        assertThat(geoms).containsExactly(polygon);
    }

    @Test
    public void should_split_polygon_in_4() throws Exception {
        Polygon polygon = polygon(
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(0.0, 0.0));

        List<Geometry> geoms = LargePolygonSplitter.split(polygon, 0.4);

        assertThat(geoms).extracting(Geometry::toString).containsExactly(
                "POLYGON ((0.5 0, 0 0, 0 0.5, 0.5 0.5, 0.5 0))",
                "POLYGON ((0 1, 0.5 1, 0.5 0.5, 0 0.5, 0 1))",
                "POLYGON ((1 1, 1 0.5, 0.5 0.5, 0.5 1, 1 1))",
                "POLYGON ((1 0, 0.5 0, 0.5 0.5, 1 0.5, 1 0))");
    }

    @Test
    public void should_split_polygon_in_16() throws Exception {
        Polygon polygon = polygon(
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(0.0, 0.0));

        List<Geometry> geoms = LargePolygonSplitter.split(polygon, 0.1);

        assertThat(geoms).extracting(Geometry::toString).hasSize(16).contains(
                "POLYGON ((0.25 0, 0 0, 0 0.25, 0.25 0.25, 0.25 0))",
                "POLYGON ((0 0.25, 0 0.5, 0.25 0.5, 0.25 0.25, 0 0.25))",
                "POLYGON ((0.25 0.5, 0.5 0.5, 0.5 0.25, 0.25 0.25, 0.25 0.5))");
    }

    private static Polygon polygon(Coordinate... coordinates) {
        return gf.createPolygon(coordinates);
    }
}
