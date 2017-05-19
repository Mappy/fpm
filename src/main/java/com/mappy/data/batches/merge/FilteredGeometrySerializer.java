package com.mappy.data.batches.merge;

import com.mappy.data.batches.utils.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;

import java.io.IOException;
import java.util.*;

import org.openstreetmap.osmosis.core.domain.v0_6.*;

public class FilteredGeometrySerializer implements GeometrySerializer {
    private final STRtree tree;
    private final GeometrySerializer serializer;
    private final MultiPolygon poly;

    public FilteredGeometrySerializer(TomtomWorld tomtom, GeometrySerializer serializer, MultiPolygon poly) {
        this.tree = new STRtree();
        for (Country country : tomtom.getCountries()) {
            Geometry geometry = country.getGeometry();
            for (Geometry geo : PolygonsUtils.polygons(geometry)) {
                List<Geometry> split = LargePolygonSplitter.split(geo, 10);
                for (Geometry g : split) {
                    tree.insert(g.getEnvelopeInternal(), g);
                }
            }
        }
        this.serializer = serializer;
        this.poly = poly;
    }

    @Override
    public long writeRelation(List<RelationMember> members, Map<String, String> tags) {
        return serializer.writeRelation(members, tags);
    }

    @Override
    public Way write(LineString line, Map<String, String> tags) {
        delegate(line, tags);
        return null;
    }

    @Override
    public void write(MultiLineString polygon, Map<String, String> tags) {
        delegate(polygon, tags);
    }

    @SuppressWarnings("unchecked")
    private void delegate(Geometry polygon, Map<String, String> tags) {
        if (!poly.contains(polygon) && !"ferry".equals(tags.get("route")) || !poly.intersects(polygon) && "ferry".equals(tags.get("route"))) {
            Geometry difference = PolygonsUtils.difference(polygon, tree.query(polygon.getEnvelopeInternal())).difference(poly);
            if (!difference.isEmpty()) {
                if (difference instanceof MultiLineString) {
                    serializer.write((MultiLineString) difference, tags);
                }
                else if (difference instanceof MultiPolygon) {
                    serializer.write((MultiPolygon) difference, tags);
                }
                else if (difference instanceof Polygon) {
                    serializer.write((Polygon) difference, tags);
                }
                else if (difference instanceof Point) {
                    serializer.write((Point) difference, tags);
                }
                else if (difference instanceof LineString) {
                    serializer.write((LineString) difference, tags);
                }
                else {
                    throw new RuntimeException(difference.getClass().getName());
                }
            }
        }
    }

    @Override
    public void write(MultiPolygon multiPolygon, Map<String, String> tags) {
        delegate(multiPolygon, tags);
    }

    @Override
    public void write(Polygon polygon, Map<String, String> tags) {
        delegate(polygon, tags);
    }

    @Override
    public void write(Point point, Map<String, String> tags) {
        delegate(point, tags);
    }

    @Override
    public void close() throws IOException {
        serializer.close();
    }
}