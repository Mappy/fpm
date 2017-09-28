package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.RelationMember;
import net.morbz.osmonaut.osm.Tags;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoundariesA0ShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {

        NameProvider nameProvider = mock(NameProvider.class);
        Map<String, String> names = newHashMap();
        names.putAll(of("name", "Andorra", "name:fr", "Andorre"));
        when(nameProvider.getAlternateNames(10200000000008L)).thenReturn(names);

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a0.shp")).thenReturn("src/test/resources/tomtom/boundaries/a0/andorra______________a0.shp");

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("andorra", "0")).thenReturn("2");

        BoundariesA0Shapefile shapefile = new BoundariesA0Shapefile(tomtomFolder, nameProvider, osmLevelGenerator);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/andorra.osm.pbf", "Test_TU");

        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/andorra.osm.pbf"));
    }

    @Test
    public void should_have_relations_with_all_tags() throws Exception {
        List<Relation> relations = pbfContent.getRelations();
        assertThat(relations).hasSize(1);

        Tags tags = relations.get(0).getTags();
        assertThat(tags.size()).isEqualTo(7);
        assertThat(tags.get("ref:tomtom")).isEqualTo("10200000000008");
        assertThat(tags.get("boundary")).isEqualTo("administrative");
        assertThat(tags.get("admin_level")).isEqualTo("2");
        assertThat(tags.get("name")).isEqualTo("Andorra");
        assertThat(tags.get("name:fr")).isEqualTo("Andorre");
        assertThat(tags.get("ref:INSEE")).isEqualTo("20");
        assertThat(tags.get("type")).isEqualTo("boundary");
    }

    @Test
    public void should_have_relations_with_ways() throws Exception {
        List<RelationMember> labels = pbfContent.getRelations().stream()//
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> relationMember.getRole().equals("outer"))//
                .collect(toList());

        assertThat(labels).hasSize(9);

        assertThat(labels.stream()
                .map(RelationMember::getEntity) //
                .filter(entity -> entity.getTags().hasKeyValue("boundary", "administrative")) //
                .filter(entity -> entity.getTags().hasKeyValue("name", "Andorra")) //
                .filter(entity -> entity.getTags().hasKeyValue("admin_level", "2")))
                .hasSize(9);
    }

    @Test
    public void should_have_relation_with_role_label_and_tags() throws Exception {
        List<RelationMember> labels = pbfContent.getRelations().stream()//
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> relationMember.getRole().equals("label"))//
                .collect(toList());

        assertThat(labels).hasSize(1);

        Tags tags = labels.get(0).getEntity().getTags();
        assertThat(tags.size()).isEqualTo(4);
        assertThat(tags.get("name")).isEqualTo("Andorra");
        assertThat(tags.get("name:fr")).isEqualTo("Andorre");
        assertThat(tags.get("ref:INSEE")).isEqualTo("20");
        assertThat(tags.get("ref:tomtom")).isEqualTo("10200000000008");
    }

    @Test
    public void should_not_have_a_null_or_empty_population_on_relation() throws Exception {
        assertThat(pbfContent.getRelations()).filteredOn(relation -> relation.getTags().hasKey("population")).isEmpty();
    }
}
