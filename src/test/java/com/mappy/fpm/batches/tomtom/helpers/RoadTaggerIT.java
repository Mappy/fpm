package com.mappy.fpm.batches.tomtom.helpers;

import com.google.inject.Guice;
import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.RelationMember;
import net.morbz.osmonaut.osm.Way;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static com.mappy.fpm.batches.utils.CollectionUtils.streamIterator;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

public class RoadTaggerIT extends AbstractTest {
    private static PbfContent pbfContent;

    @BeforeClass
    public static void getGeneratedPbf() throws IOException {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target/tests", "target/tests/splitter", TollsFactory.create("src/test/resources/osmgenerator"), "andand")).getInstance(Tomtom2Osm.class);
        launcher.run();
        pbfContent = read(new File("target/tests/andand.osm.pbf"));
    }

    @Test
    public void should_generate_roads_file() {
        Map<String, List<Way>> waysByName = pbfContent.getWays().stream().collect(groupingBy(w -> "" + w.getTags().get("name")));

        Way way = waysByName.get("Carrer de Sant Esteve").get(0);
        assertThat(way.getNodes().size()).isGreaterThanOrEqualTo(2);
        assertThat(way.getTags().get("oneway")).isEqualTo("yes");
        assertThat(way.getTags().get("name")).isEqualTo("Carrer de Sant Esteve");
    }

    @Test
    public void should_generate_roads_file_with_parking_aisle() {
        Optional<Way> way = pbfContent.getWays().stream().filter(w -> w.getTags().hasKeyValue("ref:tomtom", "10200000004654")).findFirst();

        assertThat(way.isPresent()).isTrue();
        way.ifPresent(way1 -> assertThat(way1.getTags().get("service")).isEqualTo("parking_aisle"));
    }

    @Test
    public void should_generate_roads_with_speed_profile() {
        Map<String, List<Way>> waysByName = pbfContent.getWays().stream().collect(groupingBy(w -> "" + w.getTags().get("name")));

        Way way = waysByName.get("Avinguda del Consell d'Europa").get(0);
        assertThat(way.getNodes().size()).isGreaterThanOrEqualTo(2);
        assertThat(way.getTags().get("name")).isEqualTo("Avinguda del Consell d'Europa");
        assertThat(way.getTags().get("maxspeed")).isEqualTo("40");
        assertThat(way.getTags().get("reversed:tomtom")).isEqualTo("yes");
        assertThat(way.getTags().get("mappy_sp_negative_weekend")).isEqualTo("29");
        assertThat(way.getTags().get("mappy_sp_negative_profile1")).isEqualTo(
                "43:0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_2_2_2_2_2_2_2_2_3_3_3_3_3_4_4_4_4_4_5_5_5_5_6_6_6_7_7_7_8_8_9_9_9_10_10_11_11_11_12_12_12_13_13_13_14_14_14_14_14_15_15_15_15_16_16_16_16_16_16_17_17_17_17_17_17_17_17_17_17_17_17_17_17_16_16_16_16_16_16_16_16_16_16_16_15_15_15_15_15_15_15_15_14_14_14_14_14_14_13_13_13_13_13_12_12_12_12_12_11_11_11_11_11_11_10_10_10_10_10_10_10_9_9_9_9_9_9_8_8_8_8_8_7_7_7_7_7_7_7_7_7_7_6_6_6_6_6_6_6_6_6_6_6_6_6_5_5_5_5_5_5_5_5_5_5_5_5_5_4_4_4_4_3_3_3_2_2_1_1_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0");
        assertThat(way.getTags().get("mappy_sp_negative_profile2")).isEqualTo(
                "31:0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_2_2_2_2_2_3_3_3_4_4_5_5_5_6_6_7_7_7_8_8_8_9_9_9_10_10_10_11_11_11_11_11_12_12_12_12_12_13_13_13_13_13_13_13_14_14_14_14_14_14_14_14_14_14_14_14_14_14_14_14_13_13_13_13_12_12_12_12_11_11_11_10_10_10_10_9_9_9_9_9_9_9_9_9_8_8_8_8_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_8_8_8_8_8_8_8_7_7_7_7_7_6_6_6_6_5_5_5_5_5_4_4_4_4_4_4_4_4_4_4_3_3_3_3_3_3_2_2_2_1_1_1_1_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0");
        assertThat(way.getTags().get("mappy_sp_negative_profile3")).isEqualTo(
                "42:0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_1_2_2_2_2_2_2_3_3_3_3_4_4_5_5_6_7_7_8_8_9_10_10_11_11_11_11_11_11_11_11_11_11_10_10_10_10_9_9_9_8_8_8_8_8_8_7_7_7_7_7_7_7_7_7_7_7_7_7_7_7_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_8_9_9_9_9_9_9_9_10_10_10_10_10_11_11_11_11_12_12_12_13_13_13_14_14_14_15_15_16_16_17_17_18_18_19_19_20_20_20_20_20_20_20_20_20_19_19_19_18_17_17_16_16_15_14_13_13_12_11_11_10_9_9_8_8_7_7_6_6_6_5_5_5_5_4_4_4_4_4_4_4_3_3_3_3_3_3_3_3_2_2_2_2_1_1_1_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0");
        assertThat(way.getTags().get("mappy_sp_negative_profile4")).isEqualTo(
                "47:0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_1_1_1_1_1_1_1_1_1_1_1_1_1_2_2_2_2_2_3_3_4_4_4_5_6_6_7_7_8_8_9_9_9_10_10_10_10_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_13_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_12_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_10_10_10_9_8_8_7_6_5_4_3_2_1_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0");
        assertThat(way.getTags().get("mappy_sp_negative_profile5")).isEqualTo(
                "37:0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_1_1_1_1_1_1_1_2_2_2_2_3_3_3_4_4_5_5_5_6_6_7_7_8_8_8_9_9_9_10_10_10_10_10_10_10_10_10_10_10_10_10_10_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_11_10_10_10_10_10_10_10_9_9_9_8_8_8_7_7_7_6_6_6_5_5_5_5_5_5_5_5_5_5_5_5_5_5_5_5_6_6_6_6_6_7_7_7_7_8_8_8_9_9_9_10_10_10_11_11_11_12_12_12_13_13_13_13_14_14_14_14_14_14_14_15_15_15_15_15_15_15_15_15_15_15_15_15_15_15_14_14_14_14_13_13_13_12_12_11_11_10_10_9_9_8_8_7_7_6_6_5_5_4_3_3_2_2_1_1_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0");
        assertThat(way.getTags().get("mappy_sp_negative_profile6")).isEqualTo(
                "45:0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_1_1_1_1_1_1_1_1_1_1_1_1_2_2_3_3_4_5_6_7_8_10_11_12_14_15_16_17_18_18_19_19_19_19_19_19_18_18_17_16_16_15_15_14_13_13_13_12_12_11_11_11_11_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_9_9_9_9_9_9_9_9_9_9_9_9_10_10_10_10_10_10_10_10_10_10_10_10_10_10_11_11_11_11_11_11_11_11_11_11_11_12_12_12_12_12_12_12_12_12_13_13_13_13_13_13_13_13_13_13_13_13_13_13_12_12_12_12_12_12_11_11_11_11_10_10_10_9_9_9_8_8_8_8_7_7_7_6_6_6_6_5_5_5_5_5_4_4_4_4_4_4_4_3_3_3_3_3_2_2_2_2_1_1_1_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0");
        assertThat(way.getTags().get("mappy_sp_negative_profile7")).isEqualTo(
                "33:0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_1_1_1_2_2_2_3_3_4_4_5_5_6_7_7_8_8_8_9_9_9_9_10_10_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_9_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_10_9_9_9_9_9_9_9_9_8_8_8_8_8_8_8_7_7_7_7_7_6_6_6_6_6_5_5_5_5_4_4_4_4_4_4_4_3_3_3_3_3_3_3_3_2_2_2_2_1_1_1_1_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0_0");
        assertThat(way.getTags().get("mappy_sp_negative_min_speed_pct_freeflow")).isEqualTo("79.1");
    }

    @Test
    public void should_generate_restrictions() {
        Map<String, List<Relation>> restrictionsByFromRoadName = pbfContent.getRelations().stream().collect(groupingBy(r -> "" + r.getMembers().get(0).getEntity().getTags().get("name")));
        Relation restrictions = restrictionsByFromRoadName.get("Carrer de Sant Salvador").get(1);

        assertThat(restrictions.getTags().get("from:tomtom")).isEqualTo("10200000000083");
        assertThat(restrictions.getTags().get("via:tomtom")).isEqualTo("10200000000134");
        assertThat(restrictions.getTags().get("to:tomtom")).isEqualTo("10200000000124");
        assertThat(restrictions.getTags().get("type")).isEqualTo("restriction");
        assertThat(restrictions.getTags().get("restriction")).isEqualTo("no_left_turn");
        RelationMember from = restrictions.getMembers().get(0);
        assertThat(from.getRole()).isEqualTo("from");
        assertThat(from.getEntity().getTags().get("name")).isEqualTo("Carrer de Sant Salvador");
        assertThat(from.getEntity().getEntityType()).isEqualTo(net.morbz.osmonaut.osm.EntityType.WAY);
        RelationMember to = restrictions.getMembers().get(2);
        assertThat(to.getRole()).isEqualTo("to");
        assertThat(to.getEntity().getEntityType()).isEqualTo(net.morbz.osmonaut.osm.EntityType.WAY);
        assertThat(to.getEntity().getTags().get("name")).isEqualTo("Carrer de Bonaventura Riberaygua");
        RelationMember via = restrictions.getMembers().get(1);
        assertThat(via.getRole()).isEqualTo("via");
        assertThat(via.getEntity().getEntityType()).isEqualTo(net.morbz.osmonaut.osm.EntityType.NODE);
    }

    @Test
    public void should_not_include_any_tomtom_tag() {
        Stream<Way> withTomomTags = pbfContent.getWays().stream().filter(way -> streamIterator(way.getTags().iterator()).anyMatch(tag -> tag.startsWith("tomtom")));

        assertThat(withTomomTags).isEmpty();
    }

    @Test
    public void should_include_parking_typed_entrance_exit_car_park() {
        assertThat(pbfContent.getWays().stream()) //
                .filteredOn(way -> way.getTags().hasKeyValue("ref:tomtom", "10200000002148")) //
                .isNotEmpty();
    }
}
