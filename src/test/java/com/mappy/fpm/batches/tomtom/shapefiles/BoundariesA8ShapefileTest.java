package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.Centroid;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.Tags;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoundariesA8ShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("a8.shp")).thenReturn("src/test/resources/tomtom/boundaries/a8/Anderlecht_Lemvig___________a8.shp");
        when(tomtomFolder.getFile("_an.dbf")).thenReturn("src/test/resources/tomtom/boundaries/a8/Anderlecht_Lemvig___________an.dbf");
        when(tomtomFolder.getFile("smnm.dbf")).thenReturn("src/test/resources/tomtom/boundaries/a8/Anderlecht_Lemvig___________smnm.dbf");

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("Anderlecht", 8)).thenReturn("8");

        TownTagger townTagger = mock(TownTagger.class);
        GeometryFactory factory = mock(GeometryFactory.class);

        Point point = new Point(new PackedCoordinateSequence.Double(new double[]{4.307077, 50.8366041}, 2), factory);
        when(townTagger.getCityCentroid(10560000718742L)).thenReturn(new Centroid(10560000718742L, "Anderlecht", "123", 8, 1, 7, point));

        Point point2 = new Point(new PackedCoordinateSequence.Double(new double[]{4.3451859, 50.8251293}, 2), factory);
        when(townTagger.getCityCentroid(10560000388234L)).thenReturn(new Centroid(10560000388234L, "Sint-Gillis", "456", 8, 1, 8, point2));

        Point point3 = new Point(new PackedCoordinateSequence.Double(new double[]{4.3134424, 50.8055758}, 2), factory);
        when(townTagger.getCityCentroid(10560000455427L)).thenReturn(new Centroid(10560000455427L, "Vorst", null, 8, 1, 8, point3));

        BoundariesA8Shapefile shapefile = new BoundariesA8Shapefile(tomtomFolder, townTagger, new NameProvider(tomtomFolder), osmLevelGenerator);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/a8.osm.pbf"));
        assertThat(pbfContent.getRelations()).hasSize(4);
    }

    @Test
    public void should_have_relations_with_all_tags() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("8");
        assertThat(tags).extracting(t -> t.get("type")).containsOnly("boundary");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst", "Lemvig");
        assertThat(tags).extracting(t -> t.get("name:fr")).filteredOn(Objects::nonNull).containsOnly("Anderlecht", "Saint-Gilles", "Forest");
        assertThat(tags).extracting(t -> t.get("name:nl")).filteredOn(Objects::nonNull).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
        assertThat(tags).extracting(t -> t.get("population")).containsOnly("116332", "50472", "55012", "20399");
        assertThat(tags).extracting(t -> t.get("ref:INSEE")).containsOnly("21001", "21013", "21007", "665");
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("10560000000250", "10560000000267", "10560000000263", "12080000000061");
    }

    @Test
    public void should_have_relations_with_tags_and_role_outer() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "outer".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst", "Lemvig");
        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("8");
    }

    @Test
    public void should_have_relation_with_role_label_and_tag_name() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "label".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("10560000000250", "10560000000267", "10560000000263", "12080000000061");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst", "Lemvig");
        assertThat(tags).extracting(t -> t.get("name:fr")).filteredOn(Objects::nonNull).containsOnly("Anderlecht", "Saint-Gilles", "Forest");
        assertThat(tags).extracting(t -> t.get("ref:INSEE")).containsOnly("21001", "21013", "21007", "665");
        assertThat(tags).extracting(t -> t.get("name:nl")).filteredOn(Objects::nonNull).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
    }

    @Test
    public void should_have_relation_with_tags_and_admin_centers() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "admin_centre".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags.size()).isEqualTo(3);
        assertThat(tags.get(0).size()).isEqualTo(6);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("Anderlecht", "Saint-Gilles", "Forest");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
        assertThat(tags).extracting(t -> t.get("capital")).containsOnly("8", "8", "8");
        assertThat(tags).extracting(t -> t.get("place")).containsOnly("city", "town", "town");
    }

    @Test
    public void should_have_Anderlecht_as_capital() {

        List<Tags> tags = pbfContent.getNodes().stream()
                .filter(node -> node.getTags().size() != 0)
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).hasSize(7);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst", "Lemvig");
        assertThat(tags).extracting(t -> t.get("name:fr")).filteredOn(Objects::nonNull).containsOnly("Anderlecht", "Anderlecht", "Saint-Gilles", "Saint-Gilles", "Forest", "Forest");
        assertThat(tags).extracting(t -> t.get("name:nl")).filteredOn(Objects::nonNull).containsOnly("Anderlecht", "Anderlecht", "Sint-Gillis", "Sint-Gillis", "Vorst", "Vorst");
    }

    @Test
    public void should_not_have_an_mixed_with_smnm() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).extracting(t -> t.get("ref:tomtom")).contains("12080000000061");
        assertThat(tags).extracting(t -> t.get("name")).contains("Lemvig");
        assertThat(tags).extracting(t -> t.get("name:da")).filteredOn(Objects::nonNull).doesNotContain("Molhøj");
    }
}