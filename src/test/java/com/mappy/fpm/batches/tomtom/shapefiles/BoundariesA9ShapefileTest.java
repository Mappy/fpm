package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.Tags;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoundariesA9ShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("a9.shp")).thenReturn("src/test/resources/tomtom/boundaries/a9/Ranst___________a9.shp");

        NameProvider nameProvider = mock(NameProvider.class);

        when(nameProvider.getAlternateNames(10560000000077L)) //
                .thenReturn(of("name", "Ranst", "name:nl", "RanstNL", "name:fr", "RanstFR"));
        when(nameProvider.getAlternateNames(10560000000078L)) //
                .thenReturn(of("name", "Broechem", "name:nl", "BroechemNL", "name:fr", "BroechemFR"));
        when(nameProvider.getAlternateNames(10560000000079L)) //
                .thenReturn(of("name", "Emblem", "name:nl", "EmblemNL", "name:fr", "EmblemFR"));
        when(nameProvider.getAlternateNames(10560000000090L)) //
                .thenReturn(of("name", "Oelegem", "name:nl", "OelegemNL", "name:fr", "OelegemFR"));

        when(nameProvider.getAlternateCityNames(10560000419571L)) //
                .thenReturn(of("name", "Ranst", "name:nl", "RanstCNL", "name:fr", "RanstCFR"));
        when(nameProvider.getAlternateCityNames(10560000309610L)) //
                .thenReturn(of("name", "Broechem", "name:nl", "BroechemCNL", "name:fr", "BroechemCFR"));
        when(nameProvider.getAlternateCityNames(10560000712819L)) //
                .thenReturn(of("name", "Emblem", "name:nl", "EmblemCNL", "name:fr", "EmblemCFR"));
        when(nameProvider.getAlternateCityNames(10560000571768L)) //
                .thenReturn(of("name", "Oelegem", "name:nl", "OelegemCNL", "name:fr", "OelegemCFR"));

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("Ranst", "8")).thenReturn("8");
        when(osmLevelGenerator.getOsmLevel("Ranst", "9")).thenReturn("9");

        TownTagger townTagger = mock(TownTagger.class);
        GeometryFactory factory = new GeometryFactory();

        Point point = new Point(new PackedCoordinateSequence.Double(new double[]{4.560886, 51.190382}, 2), factory);
        when(townTagger.get(10560000419571L)).thenReturn(new TownTagger.Centroid(10560000419571L, "Ranst", "123", 8, 1, 7, point));

        Point point2 = new Point(new PackedCoordinateSequence.Double(new double[]{4.601984, 51.181340}, 2), factory);
        when(townTagger.get(10560000309610L)).thenReturn(new TownTagger.Centroid(10560000309610L, "Broechem", "456", 8, 1, 8, point2));

        Point point3 = new Point(new PackedCoordinateSequence.Double(new double[]{4.606374, 51.162370}, 2), factory);
        when(townTagger.get(10560000712819L)).thenReturn(new TownTagger.Centroid(10560000712819L, "Emblem", null, 8, 1, 8, point3));

        Point point4 = new Point(new PackedCoordinateSequence.Double(new double[]{4.596975, 51.210989}, 2), factory);
        when(townTagger.get(10560000571768L)).thenReturn(new TownTagger.Centroid(10560000571768L, "Oelegem", "1011", 8, 1, 8, point4));

        BoundariesA9Shapefile shapefile = new BoundariesA9Shapefile(tomtomFolder, nameProvider, osmLevelGenerator, townTagger);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/a9.osm.pbf"));
        assertThat(pbfContent.getRelations()).hasSize(4);
    }

    @Test
    public void should_have_relations_with_all_tags() {
        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("9");
        assertThat(tags).extracting(t -> t.get("type")).containsOnly("boundary");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Ranst", "Broechem", "Emblem", "Oelegem");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("RanstFR", "BroechemFR", "EmblemFR", "OelegemFR");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("RanstNL", "BroechemNL", "EmblemNL", "OelegemNL");
        assertThat(tags).extracting(t -> t.get("ref:INSEE")).containsOnly("11035A", "11035C", "11035D", "11035B");
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("10560000000077", "10560000000078", "10560000000079", "10560000000090");
    }

    @Test
    public void should_have_relations_with_tags_and_role_outer() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "outer".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(18);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Ranst", "Broechem", "Emblem", "Oelegem");
        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("9");
    }

    @Test
    public void should_have_relation_with_role_label_and_tag_name() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "label".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags.get(0)).hasSize(5);
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("10560000000077", "10560000000078", "10560000000079", "10560000000090");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Ranst", "Broechem", "Emblem", "Oelegem");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("RanstFR", "BroechemFR", "EmblemFR", "OelegemFR");
        assertThat(tags).extracting(t -> t.get("ref:INSEE")).containsOnly("11035A", "11035C", "11035D", "11035B");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("RanstNL", "BroechemNL", "EmblemNL", "OelegemNL");
    }

    @Test
    public void should_have_relation_with_tags_and_admin_centers() {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "admin_center".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Ranst", "Broechem", "Emblem", "Oelegem");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("RanstCFR", "BroechemCFR", "EmblemCFR", "OelegemCFR");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("RanstCNL", "BroechemCNL", "EmblemCNL", "OelegemCNL");
        assertThat(tags).extracting(t -> t.get("capital")).containsOnly("8", "8", "8");
        assertThat(tags).extracting(t -> t.get("place")).containsOnly("city", "town", "town");
    }
}