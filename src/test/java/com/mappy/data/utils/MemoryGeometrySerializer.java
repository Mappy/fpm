package com.mappy.data.utils;

import com.google.common.collect.Lists;
import com.mappy.data.batches.utils.GeometrySerializer;
import com.vividsolutions.jts.geom.*;
import lombok.Getter;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.List;
import java.util.Map;

public class MemoryGeometrySerializer implements GeometrySerializer {
    @Getter
    private final List<Map<String, String>> points = Lists.newArrayList();
    @Getter
    private final List<Map<String, String>> polygons = Lists.newArrayList();
    @Getter
    private final List<Map<String, String>> multipolygons = Lists.newArrayList();
    @Getter
    private final List<Map<String, String>> multilinestrings = Lists.newArrayList();
    @Getter
    private final List<Map<String, String>> relations = Lists.newArrayList();
    @Getter
    private final List<Map<String, String>> linetrings = Lists.newArrayList();

    @Override
    public void write(Point point, Map<String, String> tags) {
        points.add(tags);
    }

    @Override
    public void write(MultiLineString polygon, Map<String, String> tags) {
        multilinestrings.add(tags);
    }

    @Override
    public Way write(LineString line, Map<String, String> tags) {
        linetrings.add(tags);
        return null;
    }

    @Override
    public void write(Polygon polygon, Map<String, String> tags) {
        polygons.add(tags);
    }

    @Override
    public void write(MultiPolygon multiPolygon, Map<String, String> tags) {
        multipolygons.add(tags);
    }

    @Override
    public void close() {
    }

    @Override
    public long writeRelation(List<RelationMember> members, Map<String, String> tags) {
        relations.add(tags);
        return 0;
    }
}