package com.mappy.data.batches.utils;

import com.google.inject.ImplementedBy;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

@ImplementedBy(OsmosisSerializer.class)
public interface GeometrySerializer extends Closeable {
    void write(Point point, Map<String, String> tags);

    void write(Polygon polygon, Map<String, String> tags);

    void write(MultiPolygon multiPolygon, Map<String, String> tags);

    void write(MultiLineString polygon, Map<String, String> tags);

    Way write(LineString line, Map<String, String> tags);

    long writeRelation(List<RelationMember> members, Map<String, String> tags);
}
