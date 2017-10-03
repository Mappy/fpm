package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
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

    private static NameProvider nameProvider = mock(NameProvider.class);

    @BeforeClass
    public static void setup() throws Exception {

        when(nameProvider.getAlternateNames(10560000000247L)) //
                .thenReturn(of("name", "Anderlecht", "name:nl", "AnderlechtNL", "name:fr", "AnderlechtFR"));
        when(nameProvider.getAlternateNames(10560000000262L)) //
                .thenReturn(of("name", "Sint-Gillis", "name:nl", "Sint-GillisNL", "name:fr", "Sint-GillisFR"));
        when(nameProvider.getAlternateNames(10560000000258L)) //
                .thenReturn(of("name", "Vorst", "name:nl", "VorstNL", "name:fr", "VorstFR"));

        when(nameProvider.getAlternateCityNames(10560000718742L)) //
                .thenReturn(of("name", "Anderlecht", "name:nl", "AnderlechtCNL", "name:fr", "AnderlechtCFR"));
        when(nameProvider.getAlternateCityNames(10560000388234L)) //
                .thenReturn(of("name", "Sint-Gillis", "name:nl", "Sint-GillisCNL", "name:fr", "Sint-GillisCFR"));
        when(nameProvider.getAlternateCityNames(10560000455427L)) //
                .thenReturn(of("name", "Vorst", "name:nl", "VorstCNL", "name:fr", "VorstCFR"));

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a9.shp")).thenReturn("src/test/resources/tomtom/boundaries/a9/Anderlecht___________a9.shp");

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("Anderlecht", "9")).thenReturn("9");

        TownTagger townTagger = mock(TownTagger.class);
        double[] doubles = {4.3451859, 50.8251293};
        GeometryFactory factory = mock(GeometryFactory.class);
        Point point = new Point(new PackedCoordinateSequence.Double(doubles, 2), factory);
        when(townTagger.get(10560000718742L)).thenReturn(new TownTagger.Centroid(10560000718742L, "Anderlecht", 8, 1, 7, point));
        double[] doubles2 = {4.3134424, 50.8055758};
        Point point2 = new Point(new PackedCoordinateSequence.Double(doubles2, 2), factory);
        when(townTagger.get(10560000388234L)).thenReturn(new TownTagger.Centroid(10560000388234L, "Sint-Gillis", 8, 1, 8, point2));
        double[] doubles3 = {4.307077, 50.8366041};
        Point point3 = new Point(new PackedCoordinateSequence.Double(doubles3, 2), factory);
        when(townTagger.get(10560000455427L)).thenReturn(new TownTagger.Centroid(10560000455427L, "Vorst", 8, 1, 8, point3));

        BoundariesA9Shapefile shapefile = new BoundariesA9Shapefile(tomtomFolder, nameProvider, osmLevelGenerator, townTagger);
        GeometrySerializer serializer = new OsmosisSerializer("target/tests/AnderlechtA9.osm.pbf", "Test_TU");
        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/AnderlechtA9.osm.pbf"));
        assertThat(pbfContent.getRelations()).hasSize(3);
    }

    @Test
    public void should_have_relations_with_all_tags() {
        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).hasSize(3);
        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("9");
        assertThat(tags).extracting(t -> t.get("type")).containsOnly("boundary");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("AnderlechtFR", "Sint-GillisFR", "VorstFR");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("AnderlechtNL", "Sint-GillisNL", "VorstNL");
        assertThat(tags).extracting(t -> t.get("population")).containsOnly("0", "0", "0");
        assertThat(tags).extracting(t -> t.get("ref:INSEE")).containsOnly("21001A", "21013A", "21007A");
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("10560000000247", "10560000000262", "10560000000258");
    }

    @Test
    public void should_have_relations_with_tags_and_role_outer() throws Exception {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "outer".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(17);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
        assertThat(tags).extracting(t -> t.get("boundary")).containsOnly("administrative");
        assertThat(tags).extracting(t -> t.get("admin_level")).containsOnly("9");
    }

    @Test
    public void should_have_relation_with_role_label_and_tag_name() throws Exception {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "label".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(3);
        assertThat(tags.get(0)).hasSize(6);
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("10560000000247", "10560000000262", "10560000000258");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("AnderlechtFR", "Sint-GillisFR", "VorstFR");
        assertThat(tags).extracting(t -> t.get("ref:INSEE")).containsOnly("21001A", "21013A", "21007A");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("AnderlechtNL", "Sint-GillisNL", "VorstNL");
        assertThat(tags).extracting(t -> t.get("population")).containsOnly("0", "0", "0");
    }

    @Test
    public void should_have_relation_with_tags_and_admin_centers() throws Exception {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "admin_center".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(3);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Anderlecht", "Sint-Gillis", "Vorst");
        assertThat(tags).extracting(t -> t.get("name:fr")).containsOnly("AnderlechtCFR", "Sint-GillisCFR", "VorstCFR");
        assertThat(tags).extracting(t -> t.get("name:nl")).containsOnly("AnderlechtCNL", "Sint-GillisCNL", "VorstCNL");
        assertThat(tags).extracting(t -> t.get("population")).containsOnly("0", "0", "0");
    }

}