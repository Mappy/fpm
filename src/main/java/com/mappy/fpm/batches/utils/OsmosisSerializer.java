package com.mappy.fpm.batches.utils;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.*;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

@Slf4j
public class OsmosisSerializer implements GeometrySerializer {

    private final Date date;
    private final Sink sink;
    private final OsmUser user;
    private final Set<Long> pointTracker = new LongOpenHashSet();
    private final Set<Long> wayTracker = new LongOpenHashSet();
    private final Set<Long> relationTracker = new LongOpenHashSet();

    public OsmosisSerializer(Sink sink, String userName, Date date) {
        this.sink = sink;
        this.date = date;
        this.user = new OsmUser(1, userName);
    }

    @Inject
    public OsmosisSerializer(@Named("com.mappy.fpm.serializer.output") String filename, @Named("com.mappy.fpm.serializer.username") String userName) throws FileNotFoundException {
        this(new BoundComputerAndSorterSink(new PbfSink(new FileOutputStream(filename), false)), userName, DateTime.now().toDate());
    }

    @Override
    public void write(Point point, Map<String, String> tags) {
        writePoint(point, tags);
    }

    @Override
    public Optional<Node> writePoint(Point point, Map<String, String> tags) {
        long id = geohash(0, point.getCoordinate());

        if (pointTracker.contains(id)) {
            log.warn("Rejecting a point because already present: " + tags);
            return Optional.empty();
        }

        pointTracker.add(id);
        Node node = new Node(ced(id, tags), point.getY(), point.getX());
        sink.process(new NodeContainer(node));
        return Optional.of(node);
    }

    @Override
    public Way write(LineString line, Map<String, String> tags) {
        Coordinate[] coordinates = line.getCoordinates();
        List<WayNode> wayNodes = new ArrayList<>(coordinates.length + 1);
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coordinate = coordinates[i];
            boolean start = i == 0;
            boolean end = i == coordinates.length - 1;
            int layer = Layers.layer(tags, start, end);
            if ("ferry".equals(tags.get("route")) && !start && !end) {
                while (exists(layer, coordinate)) {
                    layer++;
                }
            }
            long id = write(layer, coordinate);
            int size = wayNodes.size();
            if (size == 0 || (size > 0 && wayNodes.get(size - 1).getNodeId() != id)) {
                wayNodes.add(new WayNode(id));
            }
        }
        Way way = new Way(ced(wayId(line), tags), wayNodes);
        sink.process(new WayContainer(way));
        return way;
    }

    @Override
    public void write(Polygon polygon, Map<String, String> tags) {
        if (polygon.getNumInteriorRing() == 0) {
            write(polygon.getExteriorRing(), tags);
        } else {
            List<RelationMember> rm = newArrayList(new RelationMember(write(polygon.getExteriorRing(), newHashMap()).getId(), Way, "outer"));
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                rm.add(new RelationMember(write(polygon.getInteriorRingN(i), newHashMap()).getId(), Way, "inner"));
            }
            sink.process(new RelationContainer(new Relation(ced(wayId(polygon), addMultipolygon(tags)), rm)));
        }
    }

    @Override
    public long writeRelation(List<RelationMember> members, Map<String, String> tags) {
        for (RelationMember member : members) {
            if (member.getMemberType() == Node) {
                checkState(pointTracker.contains(member.getMemberId()), "Adding relation on missing node");
            } else if (member.getMemberType() == Way) {
                checkState(wayTracker.contains(member.getMemberId()), "Adding relation on missing way");
            }
        }
        long id = relationId(members.get(0).getMemberId());
        sink.process(new RelationContainer(new Relation(ced(id, tags), members)));
        return id;
    }

    @Override
    public void write(MultiLineString way, Map<String, String> tags) {
        for (int i = 0; i < way.getNumGeometries(); i++) {
            write((LineString) way.getGeometryN(i), tags);
        }
    }

    @Override
    public void write(MultiPolygon multiPolygon, Map<String, String> tags) {
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            write((Polygon) multiPolygon.getGeometryN(i), tags);
        }
    }

    @Override
    public void close() {
        sink.complete();
        sink.release();
    }

    private boolean exists(int layer, Coordinate coordinate) {
        return pointTracker.contains(geohash(layer, coordinate));
    }

    private long write(int layer, Coordinate coordinate) {
        long id = geohash(layer, coordinate);
        if (!pointTracker.contains(id)) {
            pointTracker.add(id);
            sink.process(new NodeContainer(new Node(new CommonEntityData(id, 1, date, user, 1L), coordinate.y, coordinate.x)));
        }
        return id;
    }

    private Long wayId(Geometry geometry) {
        long id = geohash(0, geometry.getCentroid().getCoordinate());
        while (wayTracker.contains(id)) {
            log.debug("Collision on way with {}", id);
            id++;
        }
        wayTracker.add(id);
        return id;
    }

    private long relationId(long memberId) {
        long id = memberId;
        while (relationTracker.contains(id)) {
            log.debug("Collision on relation with {}", id);
            id++;
        }
        relationTracker.add(id);
        return id;
    }

    private CommonEntityData ced(long id, Map<String, String> tags) {
        return new CommonEntityData(id, 1, date, user, 1L, tags.entrySet().stream().map(en -> new Tag(en.getKey(), en.getValue())).collect(toList()));
    }

    private static Map<String, String> addMultipolygon(Map<String, String> tags) {
        return ImmutableMap.<String, String>builder().putAll(tags).put("type", "multipolygon").build();
    }

    private static long geohash(int layer, Coordinate coordinate) {
        return Geohash.encodeGeohash(layer, coordinate.x, coordinate.y);
    }
}