package com.mappy.fpm.batches.utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LongLineSplitterTest {
    private static final GeometryFactory gf = new GeometryFactory();

    @Test
    public void should_split_long_line() throws Exception {
        Polygon polygon = polygon(
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0),
                new Coordinate(0.0, 1.0),
                new Coordinate(0.0, 0.0));

        List<LineString> lines = LongLineSplitter.split(polygon.getExteriorRing(), 3);

        assertThat(lines).containsExactly(
                linestring(
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(1.0, 1.0)),
                linestring(
                        new Coordinate(1.0, 1.0),
                        new Coordinate(0.0, 1.0),
                        new Coordinate(0.0, 0.0)));
    }

    @Test
    public void should_return_same_geom_if_necessary() throws Exception {
        LineString linestring = linestring(
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0));

        List<LineString> lines = LongLineSplitter.split(linestring, 3);

        assertThat(lines).containsExactly(
                linestring(
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(1.0, 1.0)));
        assertThat(lines).isEqualTo(LongLineSplitter.split(linestring, 4));
        assertThat(lines).isEqualTo(LongLineSplitter.split(linestring, 5));
    }

    @Test
    public void should_split_linestring() throws Exception {
        LineString linestring = linestring(
                new Coordinate(0.0, 0.0),
                new Coordinate(1.0, 0.0),
                new Coordinate(1.0, 1.0));

        List<LineString> lines = LongLineSplitter.split(linestring, 2);

        assertThat(lines).containsExactly(
                linestring(
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0)),
                linestring(
                        new Coordinate(1.0, 0.0),
                        new Coordinate(1.0, 1.0)));
    }

    private static LineString linestring(Coordinate... coordinates) {
        return gf.createLineString(coordinates);
    }

    private static Polygon polygon(Coordinate... coordinates) {
        return gf.createPolygon(coordinates);
    }
}
