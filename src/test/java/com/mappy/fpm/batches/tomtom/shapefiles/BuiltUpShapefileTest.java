package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
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

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuiltUpShapefileTest extends AbstractTest{

    private static Tomtom2OsmTestUtils.PbfContent pbfContent;


    @BeforeClass
    public static void setup() throws Exception {
        NameProvider nameProvider = mock(NameProvider.class);

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("bu.shp")).thenReturn("src/test/resources/tomtom/boundaries/bu/rougnat___________bu.shp");

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("rougnat", "10")).thenReturn("10");

        TownTagger townTagger = mock(TownTagger.class);
        GeometryFactory factory = mock(GeometryFactory.class);

        Point point = new Point(new PackedCoordinateSequence.Double(new double[]{4.560886, 51.190382}, 2), factory);
        when(townTagger.getHamlet(12500001063055L)).thenReturn(new TownTagger.Centroid(12500001063055L, "Rougnat", "123", 8, 32, 7, point));

        Point point2 = new Point(new PackedCoordinateSequence.Double(new double[]{4.601984, 51.181340}, 2), factory);
        when(townTagger.getHamlet(12500001060481L)).thenReturn(new TownTagger.Centroid(12500001060481L, "Auzances", "456", 8, 32, 8, point2));

        Point point3 = new Point(new PackedCoordinateSequence.Double(new double[]{4.606374, 51.162370}, 2), factory);
        when(townTagger.getHamlet(12500001067545L)).thenReturn(new TownTagger.Centroid(12500001067545L, "La Chaux-Bourdue", null, 8, 32, 8, point3));

        Point point4 = new Point(new PackedCoordinateSequence.Double(new double[]{4.596975, 51.210989}, 2), factory);
        when(townTagger.getHamlet(12500001060737L)).thenReturn(new TownTagger.Centroid(112500001060737L, "Le Montely", "1011", 8, 32, 8, point4));

        BuiltUpShapefile shapefile = new BuiltUpShapefile(tomtomFolder, nameProvider, osmLevelGenerator, townTagger);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/rougnat.osm.pbf", "Test_TU");
        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/rougnat.osm.pbf"));
        assertThat(pbfContent.getRelations()).hasSize(4);

    }

    @Test
    public void should_have_relations_with_all_tags() {
        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags).extracting(t -> t.get("landuse")).containsOnly("residential");
        assertThat(tags).extracting(t -> t.get("type")).containsOnly("multipolygon");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Rougnat", "Auzances", "La Chaux-Bourdue", "Le Montely");
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("12500001063055", "12500001060481", "12500001067545", "12500001060737");
    }

    @Test
    public void should_have_relations_with_tags_and_role_outer() throws Exception {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "outer".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(8);
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Rougnat", "Auzances", "La Chaux-Bourdue", "Le Montely");
        assertThat(tags).extracting(t -> t.get("place")).containsOnly("hamlet");
    }

    @Test
    public void should_have_relation_with_role_label_and_tag_name() throws Exception {

        List<Tags> tags = pbfContent.getRelations().stream()
                .flatMap(f -> f.getMembers().stream())
                .filter(relationMember -> "label".equals(relationMember.getRole()))
                .map(m -> m.getEntity().getTags())
                .collect(toList());

        assertThat(tags).hasSize(4);
        assertThat(tags.get(0)).hasSize(2);
        assertThat(tags).extracting(t -> t.get("ref:tomtom")).containsOnly("12500001063055", "12500001060481", "12500001067545", "12500001060737");
        assertThat(tags).extracting(t -> t.get("name")).containsOnly("Rougnat", "Auzances", "La Chaux-Bourdue", "Le Montely");
    }

}