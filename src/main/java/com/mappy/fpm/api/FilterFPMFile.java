package com.mappy.fpm.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mappy.fpm.batches.splitter.SplitterSink;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import crosby.binary.osmosis.OsmosisReader;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Throwables.propagate;

@Slf4j
public class FilterFPMFile {
    private final Map<Long, Node> nodes = Maps.newHashMap();
    private final Map<Long, Way> boundaries = Maps.newHashMap();
    private final List<Geometry> highways = Lists.newArrayList();
    private final List<Geometry> districtRelations = Lists.newArrayList();


    public FilterFPMFile(String filename) {
        try {
            log.info("Loading OSM {}", filename);
            for (Path path : Files.list(Paths.get(filename)).collect(Collectors.toList())) {
                OsmosisReader reader = new OsmosisReader(new FileInputStream(path.toString()));
                reader.setSink(new SplitterSink(path.toString()) {
                    @Override
                    public void process(NodeContainer node) {
                        nodes.put(node.getEntity().getId(), node.getEntity());
                    }

                    @Override
                    public void process(WayContainer way) {
                        Way entity = way.getEntity();
                        if (isanHighway(entity)) {
                            highways.add(getGeometry(entity));
                        }
                        if (hasKeyValue(entity.getTags(), "boundary", "administrative")) {
                            boundaries.put(entity.getId(), entity);
                        }
                    }

                    @Override
                    public void process(RelationContainer rel) {
                        Relation entity = rel.getEntity();
                        if (hasKeyValue(entity.getTags(), "admin_level", "4")) {

                            List<Way> waysInBoundary = getWaysInBoundary(entity);

                            districtRelations.add(getGeometry(waysInBoundary));
                        }
                    }
                });
                reader.run();
            }
        } catch (IOException e) {
            throw propagate(e);
        }
        log.info("Ways : {}", highways.size());
    }

    public List<Geometry> getDistricts() {
        return districtRelations;
    }

    public List<Geometry> getHighways() {
        return highways;
    }

    private List<Way> getWaysInBoundary(Relation entity) {
        return entity.getMembers().stream()
                .filter(relationMember -> "outer".equals(relationMember.getMemberRole()) || "inner".equals(relationMember.getMemberRole()))
                .map(relationMember -> boundaries.get(relationMember.getMemberId()))
                .collect(Collectors.toList());
    }

    private boolean hasKeyValue(Collection<Tag> tags, String boundary, String administrative) {
        return tags.stream().anyMatch(tag -> boundary.equals(tag.getKey()) && administrative.equals(tag.getValue()));
    }

    private boolean isanHighway(Way entity) {
        return entity.getTags().stream().anyMatch(tag -> "highway".equals(tag.getKey()));
    }

    private Coordinate getCoordinate(WayNode n) {
        Node node = nodes.get(n.getNodeId());
        return new Coordinate(node.getLatitude(), node.getLongitude());
    }

    private LineString getGeometry(Way entity) {
        GeometryFactory fact = new GeometryFactory();

        return fact.createLineString(new CoordinateArraySequence(entity.getWayNodes().stream()
                .map(this::getCoordinate)
                .toArray(Coordinate[]::new)));
    }

    private Polygon getGeometry(List<Way> entity) {
        GeometryFactory fact = new GeometryFactory();

        List<Coordinate> coordinates = entity.stream()
                .flatMap(way -> way.getWayNodes().stream())
                .map(this::getCoordinate)
                .collect(Collectors.toList());

        coordinates.add(coordinates.get(0));

        LinearRing linearRing = fact.createLinearRing(new CoordinateArraySequence(coordinates.toArray(new Coordinate[coordinates.size()])));

        return fact.createPolygon(linearRing);
    }

}
