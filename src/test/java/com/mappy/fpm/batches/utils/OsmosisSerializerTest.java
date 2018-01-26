package com.mappy.fpm.batches.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.*;
import lombok.Getter;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class OsmosisSerializerTest {
    private static final GeometryFactory gf = new GeometryFactory();
    private static final Date timestamp = new Date();
    private final MemorySink sink = new MemorySink();
    private final OsmosisSerializer serializer = new OsmosisSerializer(sink, "user", timestamp);

    @Test
    public void should_write_points() {
        serializer.write(point(42.0), ImmutableMap.of("place", "city", "name", "Paris"));
        serializer.write(point(42.1), ImmutableMap.of("place", "town"));

        assertThat(sink.getEntities()).containsExactly(
                new Node(new CommonEntityData(1156149936395291L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("place", "city"), new Tag("name", "Paris"))), 42.0, 3.0),
                new Node(new CommonEntityData(1155588746470758L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("place", "town"))), 42.1, 3.0));
    }

    @Test
    public void should_refuse_to_forget_a_point() {
        serializer.writePoint(point(42.0), ImmutableMap.of("place", "city"));

        Optional<Node> node2 = serializer.writePoint(point(42.0), ImmutableMap.of("amenity", "hotel"));
        assertThat(node2).isEmpty();
    }

    @Test
    public void should_not_write_duplicate_points_on_ways() {
        serializer.write(
                linestring(
                        new Coordinate[]{
                                new Coordinate(0.0, 0.0),
                                new Coordinate(1.0, 0.0),
                                new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());
        serializer.write(
                linestring(
                        new Coordinate[]{
                                new Coordinate(-1.0, 0.0),
                                new Coordinate(1.0, 0.0),
                                new Coordinate(3.0, 0.0)}),
                Maps.newHashMap());

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Node).hasSize(5);
        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Way).hasSize(2);
    }

    @Test
    public void should_increment_way_id_if_already_exists() {

        serializer.write(
                linestring(
                        new Coordinate[]{
                                new Coordinate(0.0, 0.0),
                                new Coordinate(1.0, 1.0)}),
                Maps.newHashMap());
        serializer.write(
                linestring(
                        new Coordinate[]{
                                new Coordinate(1.0, 1.0),
                                new Coordinate(0.0, 0.0)}),
                Maps.newHashMap());

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Way).extracting(Entity::getId).containsExactly(
                1525972802595572L,
                1525972802595573L);
    }

    @Test
    public void should_generate_relation_id() {
        writeWays();
        serializer.write(newArrayList(
                new RelationMember(1525972802595572L, Way, "from"),
                new RelationMember(1525972802595573L, Way, "to")), newHashMap());
        serializer.write(newArrayList(
                new RelationMember(1525972802595573L, Way, "to"),
                new RelationMember(1525972802595572L, Way, "from")), newHashMap());

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Relation).extracting(Entity::getId).containsExactly(
                1525972802595572L,
                1525972802595573L);
    }

    @Test
    public void should_generate_relation_id_with_same_way() {
        writeWays();
        serializer.write(newArrayList(
                new RelationMember(1525972802595572L, Way, "from")), newHashMap());
        serializer.write(newArrayList(
                new RelationMember(1525972802595573L, Way, "to")), newHashMap());

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Relation).extracting(Entity::getId).containsExactly(
                1525972802595572L,
                1525972802595573L);
    }

    @Test
    public void should_not_generate_same_relation_id_with_layer() {
        writeWays();
        serializer.write(newArrayList(
                new RelationMember(1525972802595572L, Way, "from")), ImmutableMap.of("layer", "0"));
        serializer.write(newArrayList(
                new RelationMember(1525972802595572L, Way, "to")), ImmutableMap.of("layer", "2"));

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Relation).extracting(Entity::getId).containsExactly(
                1525972802595572L,
                1525972802595575L);
    }

    @Test
    public void should_write_ways() {
        serializer.write(
                multilinestring(
                        new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0), new Coordinate(2.0, 0.0)},
                        new Coordinate[]{new Coordinate(2.0, 0.0), new Coordinate(3.0, 0.0)}),
                Maps.newHashMap());

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Node).hasSize(4);
        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Way).hasSize(2);
    }

    @Test
    public void should_write_boundary() {
        serializer.writeBoundary(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());

        serializer.writeBoundary(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 1.0),
                        new Coordinate(1.0, 1.0),
                        new Coordinate(0.0, 2.0)}),
                Maps.newHashMap());

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Node).hasSize(6);
        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Way).hasSize(2);
    }

    @Test
    public void should_not_write_same_boundary() {
        serializer.writeBoundary(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());

        serializer.writeBoundary(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Node).hasSize(3);
        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Way).hasSize(1);
    }

    @Test
    public void should_not_write_same_boundary_but_return_same_id_of_way() {
        Optional<Long> boundaryId1 = serializer.writeBoundary(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());

        Optional<Long> boundaryId2 = serializer.writeBoundary(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());

        assertThat(boundaryId1).isEqualTo(boundaryId2);
    }

    @Test
    public void should_avoid_to_have_boundary_and_ways_with_similar_ids_for_same_geometry() {
        serializer.writeBoundary(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());

        serializer.write(linestring(
                new Coordinate[]{
                        new Coordinate(0.0, 0.0),
                        new Coordinate(1.0, 0.0),
                        new Coordinate(2.0, 0.0)}),
                Maps.newHashMap());

        List<Long> twoWays = sink.getEntities()
                .stream()
                .filter(entity -> entity instanceof Way)
                .map(Entity::getId)
                .collect(Collectors.toList());

        assertThat(twoWays.get(0) - twoWays.get(1)).isGreaterThan(1);
    }

    @Test
    public void should_write_polygon() {
        serializer.write(
                polygon(new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0), new Coordinate(1.0, 1.0), new Coordinate(0.0, 1.0), new Coordinate(0.0, 0.0)),
                ImmutableMap.of("landuse", "residential"));

        assertThat(sink.getEntities()).containsExactly(
                node(1525412777012587L, 0.0, 0.0),
                node(1525143032931440L, 0.0, 1.0),
                node(1525778252023487L, 1.0, 1.0),
                node(1525335635521827L, 1.0, 0.0),
                new Way(new CommonEntityData(1525972802595572L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("landuse", "residential"))), newArrayList(
                        new WayNode(1525412777012587L),
                        new WayNode(1525143032931440L),
                        new WayNode(1525778252023487L),
                        new WayNode(1525335635521827L),
                        new WayNode(1525412777012587L))));
    }

    @Test
    public void should_discard_consecutive_duplicated_points() {
        serializer.write(
                polygon(new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0), new Coordinate(1.0, 1.0), new Coordinate(1.0000000001, 1.0), new Coordinate(0.0, 1.0), new Coordinate(0.0, 0.0)),
                ImmutableMap.of("landuse", "residential"));

        assertThat(sink.getEntities()).containsExactly(
                node(1525412777012587L, 0.0, 0.0),
                node(1525143032931440L, 0.0, 1.0),
                node(1525778252023487L, 1.0, 1.0),
                node(1525335635521827L, 1.0, 0.0),
                new Way(new CommonEntityData(1525972802595572L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("landuse", "residential"))), newArrayList(
                        new WayNode(1525412777012587L),
                        new WayNode(1525143032931440L),
                        new WayNode(1525778252023487L),
                        new WayNode(1525335635521827L),
                        new WayNode(1525412777012587L))));
    }

    @Test
    public void should_write_polygon_with_holes() {
        serializer.write(
                polygon(
                        new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(10.0, 0.0), new Coordinate(10.0, 10.0), new Coordinate(0.0, 10.0), new Coordinate(0.0, 0.0)},
                        new Coordinate[][]{new Coordinate[]{new Coordinate(2.0, 2.0), new Coordinate(3.0, 2.0), new Coordinate(3.0, 3.0), new Coordinate(2.0, 3.0), new Coordinate(2.0, 2.0)},
                                new Coordinate[]{new Coordinate(6.0, 6.0), new Coordinate(7.0, 6.0), new Coordinate(7.0, 7.0), new Coordinate(6.0, 7.0), new Coordinate(6.0, 6.0)}}),
                ImmutableMap.of("natural", "water"));

        assertThat(sink.getEntities())
                .contains(
                        new Relation(new CommonEntityData(1534980890946047L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("natural", "water"), new Tag("type", "multipolygon"))),
                                newArrayList(
                                        new RelationMember(1534980219189183L, Way, "outer"),
                                        new RelationMember(1541218125151719L, Way, "inner"),
                                        new RelationMember(1668015227122919L, Way, "inner"))));

    }

    @Test
    public void should_write_polygon_without_holes_as_way() {
        serializer.write(
                polygon(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0), new Coordinate(1.0, 1.0), new Coordinate(0.0, 1.0), new Coordinate(0.0, 0.0)}, new Coordinate[][]{}),
                ImmutableMap.of("natural", "water"));

        assertThat(sink.getEntities()).containsExactly(
                node(1525412777012587L, 0.0, 0.0),
                node(1525143032931440L, 0.0, 1.0),
                node(1525778252023487L, 1.0, 1.0),
                node(1525335635521827L, 1.0, 0.0),
                new Way(new CommonEntityData(1525972802595572L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("natural", "water"))), newArrayList(
                        new WayNode(1525412777012587L),
                        new WayNode(1525143032931440L),
                        new WayNode(1525778252023487L),
                        new WayNode(1525335635521827L),
                        new WayNode(1525412777012587L))));
    }

    @Test
    public void should_write_multipolygon() {
        Polygon polygon1 = polygon(
                new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(10.0, 0.0), new Coordinate(10.0, 10.0), new Coordinate(0.0, 10.0), new Coordinate(0.0, 0.0)},
                new Coordinate[][]{new Coordinate[]{new Coordinate(2.0, 2.0), new Coordinate(3.0, 2.0), new Coordinate(3.0, 3.0), new Coordinate(2.0, 3.0), new Coordinate(2.0, 2.0)}});

        Polygon polygon2 = polygon(
                new Coordinate[]{new Coordinate(20.0, 20.0), new Coordinate(30.0, 20.0), new Coordinate(30.0, 30.0), new Coordinate(20.0, 30.0), new Coordinate(20.0, 20.0)},
                new Coordinate[][]{new Coordinate[]{new Coordinate(22.0, 22.0), new Coordinate(23.0, 22.0), new Coordinate(23.0, 23.0), new Coordinate(22.0, 23.0), new Coordinate(22.0, 22.0)}});

        serializer.write(gf.createMultiPolygon(new Polygon[]{polygon1, polygon2}), ImmutableMap.of("natural", "water"));

        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Node).hasSize(16);
        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Way).hasSize(4);
        assertThat(sink.getEntities()).filteredOn(e -> e instanceof Relation).containsExactly(
                new Relation(new CommonEntityData(1534981437239028L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("natural", "water"), new Tag("type", "multipolygon"))),
                        newArrayList(
                                new RelationMember(1534980219189183L, Way, "outer"),
                                new RelationMember(1541218125151719L, Way, "inner"))),
                new Relation(new CommonEntityData(1189390193519277L, 1, timestamp, new OsmUser(1, "user"), 1L, newArrayList(new Tag("natural", "water"), new Tag("type", "multipolygon"))),
                        newArrayList(
                                new RelationMember(1189374404263399L, Way, "outer"),
                                new RelationMember(1173569056124267L, Way, "inner"))));
    }

    private void writeWays() {
        serializer.write(
                linestring(
                        new Coordinate[]{
                                new Coordinate(0.0, 0.0),
                                new Coordinate(1.0, 1.0)}),
                Maps.newHashMap());
        serializer.write(
                linestring(
                        new Coordinate[]{
                                new Coordinate(0.0, 0.0),
                                new Coordinate(1.0, 1.0)}),
                Maps.newHashMap());
    }

    private static Node node(long id, double lat, double lon) {
        return new Node(new CommonEntityData(id, 1, timestamp, new OsmUser(1, "user"), 1L), lat, lon);
    }

    private static LineString linestring(Coordinate[] first) {
        return gf.createLineString(first);
    }

    private static MultiLineString multilinestring(Coordinate[] first, Coordinate[] second) {
        return gf.createMultiLineString(new LineString[]{gf.createLineString(first), gf.createLineString(second)});
    }

    private static Polygon polygon(Coordinate[] exterior, Coordinate[][] interior) {
        LinearRing[] rings = new LinearRing[interior.length];
        for (int i = 0; i < rings.length; i++) {
            rings[i] = gf.createLinearRing(interior[i]);
        }
        return gf.createPolygon(gf.createLinearRing(exterior), rings);
    }

    private static Polygon polygon(Coordinate... coordinates) {
        return gf.createPolygon(coordinates);
    }

    private static Point point(double lat) {
        return gf.createPoint(new Coordinate(3.0, lat));
    }

    public static class MemorySink implements Sink {
        @Getter
        private final List<Entity> entities = Lists.newArrayList();

        @Override
        public void initialize(Map<String, Object> metaData) {
        }

        @Override
        public void complete() {
        }

        @Override
        public void release() {
        }

        @Override
        public void process(EntityContainer entityContainer) {
            entities.add(entityContainer.getEntity());
        }
    }
}
