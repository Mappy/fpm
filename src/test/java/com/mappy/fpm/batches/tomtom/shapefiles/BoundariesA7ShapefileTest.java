package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger.Centroid;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.RelationMember;
import net.morbz.osmonaut.osm.Tags;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoundariesA7ShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a7.shp")).thenReturn("src/test/resources/tomtom/boundaries/a7/b___a7.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(10560000000808L)).thenReturn(of("name", "Brussel Hoofdstad", "name:fr", "Brussel Hoofdstad FR", "name:nl", "Brussel Hoofdstad NL"));
        when(nameProvider.getAlternateNames(10560000000823L)).thenReturn(of("name", "Halle-Vilvoorde", "name:fr", "Halle-Vilvoorde FR", "name:nl", "Halle-Vilvoorde NL"));

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("b", "0")).thenReturn("2");
        when(osmLevelGenerator.getOsmLevel("b", "7")).thenReturn("7");

        TownTagger townTagger = mock(TownTagger.class);
        Point point = new Point(new PackedCoordinateSequence.Double(new double[]{4.307077, 50.8366041}, 2), new GeometryFactory());
        Point point2 = new Point(new PackedCoordinateSequence.Double(new double[]{4.232918, 50.737785}, 2), new GeometryFactory());
        when(townTagger.getCapital(7)).thenReturn(newArrayList(
                new Centroid(10560022000808L, "Brussel Hoofdstad", "21000", 0, 1, 2, point),
                new Centroid(10560033000808L, "Halle", "23000", 7, 1, 10, point2)));

        BoundariesA7Shapefile shapefile = new BoundariesA7Shapefile(tomtomFolder, nameProvider, osmLevelGenerator, townTagger);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/a7.osm.pbf"));
    }

    @Test
    public void should_have_members_with_tags() {

        Relation brussel = pbfContent.getRelations().stream().filter(member -> member.getTags().hasKeyValue("ref:tomtom", "10560000000808")).findFirst().get();
        assertThat(brussel.getTags().size()).isEqualTo(11);
        assertThat(brussel.getTags().get("name")).isEqualTo("Brussel Hoofdstad");
        assertThat(brussel.getTags().get("name:fr")).isEqualTo("Brussel Hoofdstad FR");
        assertThat(brussel.getTags().get("name:nl")).isEqualTo("Brussel Hoofdstad NL");
        assertThat(brussel.getTags().get("boundary")).isEqualTo("administrative");
        assertThat(brussel.getTags().get("capital")).isEqualTo("yes");
        assertThat(brussel.getTags().get("place")).isEqualTo("city");
        assertThat(brussel.getTags().get("ref:INSEE")).isEqualTo("21000");
        assertThat(brussel.getTags().get("type")).isEqualTo("boundary");
        assertThat(brussel.getTags().get("admin_level")).isEqualTo("7");
        assertThat(brussel.getTags().get("layer")).isEqualTo("7");
    }

    @Test
    public void should_have_relation_with_role_label_and_tags() {
        Tags labels = pbfContent.getRelations().stream()//
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> relationMember.getRole().equals("label"))//
                .map(RelationMember::getEntity)
                .map(Entity::getTags)
                .findFirst().get();

        assertThat(labels.size()).isEqualTo(5);
        assertThat(labels.get("name")).isEqualTo("Brussel Hoofdstad");
        assertThat(labels.get("name:fr")).isEqualTo("Brussel Hoofdstad FR");
        assertThat(labels.get("name:nl")).isEqualTo("Brussel Hoofdstad NL");
        assertThat(labels.get("ref:INSEE")).isEqualTo("21000");
    }

    @Test
    public void should_have_relation_with_tags_and_admin_centers() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "admin_center".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Brussel Hoofdstad", "Halle-Vilvoorde");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("Brussel Hoofdstad FR", "Halle-Vilvoorde FR");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("Brussel Hoofdstad NL", "Halle-Vilvoorde NL");
        assertThat(tags).extracting(t -> t.get("capital")).containsOnly("yes", "7");
        assertThat(tags).extracting(t -> t.get("place")).containsOnly("city", "town");
    }
}