package com.mappy.fpm.batches.utils;

import com.google.inject.ImplementedBy;
import com.vividsolutions.jts.geom.*;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ImplementedBy(OsmosisSerializer.class)
public interface GeometrySerializer extends Closeable {
    void write(Point point, Map<String, String> tags);

    default Optional<Node> writePoint(Point point, Map<String, String> tags) {
        throw new UnsupportedOperationException();
    }

    void write(Polygon polygon, Map<String, String> tags);

    void write(MultiPolygon multiPolygon, Map<String, String> tags);

    void write(MultiLineString polygon, Map<String, String> tags);

    Way write(LineString line, Map<String, String> tags);

    long writeRelation(List<RelationMember> members, Map<String, String> tags);
}
