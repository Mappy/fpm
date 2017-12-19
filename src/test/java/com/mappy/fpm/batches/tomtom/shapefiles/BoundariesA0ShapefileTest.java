package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.CapitalProvider;
import com.mappy.fpm.batches.tomtom.helpers.Centroid;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.RelationMember;
import net.morbz.osmonaut.osm.Tags;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoundariesA0ShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a0.shp")).thenReturn("src/test/resources/tomtom/boundaries/a0/andorra______________a0.shp");

        CapitalProvider capitalProvider = mock(CapitalProvider.class);
        Point point = new Point(new PackedCoordinateSequence.Double(new double[]{1.52185, 42.50760}, 2), new GeometryFactory());
        Centroid capital = new Centroid(10560000718742L, "Capital Name", "123", 0, 1, 7, point);
        when(capitalProvider.get(0)).thenReturn(newArrayList(capital));

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(10200000000008L)).thenReturn(of("name", "Andorra", "name:fr", "Andorre"));

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("andorra", 0)).thenReturn("2");

        BoundariesA0Shapefile shapefile = new BoundariesA0Shapefile(tomtomFolder, capitalProvider, nameProvider, osmLevelGenerator);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/a0.osm.pbf"));
    }

    @Test
    public void should_have_relations_with_all_tags() {
        List<Relation> relations = pbfContent.getRelations();
        assertThat(relations).hasSize(1);

        Tags tags = relations.get(0).getTags();
        assertThat(tags.size()).isEqualTo(8);
        assertThat(tags.get("ref:tomtom")).isEqualTo("10200000000008");
        assertThat(tags.get("boundary")).isEqualTo("administrative");
        assertThat(tags.get("admin_level")).isEqualTo("2");
        assertThat(tags.get("layer")).isEqualTo("2");
        assertThat(tags.get("name")).isEqualTo("Andorra");
        assertThat(tags.get("name:fr")).isEqualTo("Andorre");
        assertThat(tags.get("ref:INSEE")).isEqualTo("20");
        assertThat(tags.get("type")).isEqualTo("boundary");
    }

    @Test
    public void should_have_relations_with_ways() {
        List<RelationMember> labels = pbfContent.getRelations().stream()//
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> "outer".equals(relationMember.getRole()))//
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
    public void should_have_relation_with_role_label_and_tags() {
        List<RelationMember> labels = pbfContent.getRelations().stream()//
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> "label".equals(relationMember.getRole()))//
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
    public void should_have_relation_with_role_admin_center_and_tags() {
        List<RelationMember> adminCenter = pbfContent.getRelations().stream()//
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> "admin_center".equals(relationMember.getRole()))//
                .collect(toList());

        assertThat(adminCenter).hasSize(1);

        Tags tags = adminCenter.get(0).getEntity().getTags();
        assertThat(tags.size()).isEqualTo(3);
        assertThat(tags.get("name")).isEqualTo("Capital Name");
        assertThat(tags.get("capital")).isEqualTo("yes");
        assertThat(tags.get("place")).isEqualTo("city");
    }

    @Test
    public void should_not_have_a_null_or_empty_population_on_relation() {
        assertThat(pbfContent.getRelations()).filteredOn(relation -> relation.getTags().hasKey("population")).isEmpty();
    }
}
