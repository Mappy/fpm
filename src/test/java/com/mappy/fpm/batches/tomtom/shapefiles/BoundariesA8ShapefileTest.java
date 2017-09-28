package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger.Centroid;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BoundariesA8ShapefileTest {

    private static Tomtom2OsmTestUtils.PbfContent pbfContent;

    private static NameProvider nameProvider = mock(NameProvider.class);

    @BeforeClass
    public static void setup() throws Exception {
        Path dir = get("target", "tests");
        if(!dir.toFile().exists()) {
            createDirectory(dir);
        }

        Map<String, String> names = newHashMap();
        names.putAll(of("name", "Brussel", "name:nl", "Bruxelles", "name:fr", "Bruxelles"));
        when(nameProvider.getAlternateNames(10560000000250L)).thenReturn(names);

        Map<String, String> names2 = newHashMap();
        names2.putAll(of("name", "Brussel", "name:nl", "Bruxelles", "name:fr", "Bruxelles"));
        when(nameProvider.getAlternateNames(10560000000267L)).thenReturn(names2);

        Map<String, String> names3 = newHashMap();
        names3.putAll(of("name", "Brussel", "name:nl", "Bruxelles", "name:fr", "Bruxelles"));
        when(nameProvider.getAlternateNames(10560000000263L)).thenReturn(names3);

        Map<String, String> names4 = newHashMap();
        names4.putAll(of("name", "Brussel", "name:nl", "Bruxelles", "name:fr", "Bruxelles"));
        when(nameProvider.getAlternateNames(10560000718742L)).thenReturn(names4);


        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a8.shp")).thenReturn("src/test/resources/tomtom/boundaries/a8/belbe3___________a8.shp");

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("belbe3", "8")).thenReturn("8");

        TownTagger townTagger = mock(TownTagger.class);
        double[] doubles3 = {4.307077, 50.8366041};
        GeometryFactory factory = mock(GeometryFactory.class);
        Point point = new Point(new PackedCoordinateSequence.Double(doubles3, 2), factory);
        when(townTagger.get(10560000718742L)).thenReturn(new Centroid(10560000718742L, "toto", 8, 1, 7, point));

        BoundariesA8Shapefile shapefile = new BoundariesA8Shapefile(tomtomFolder, nameProvider, osmLevelGenerator, townTagger);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/belbe3.osm.pbf", "Test_TU");

        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/belbe3.osm.pbf"));
    }

    @Test
    public void should_have_relations_with_ways() throws Exception {
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("outer")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("boundary")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("name")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("admin_level", "8")).isNotEmpty();
    }

    @Test
    public void should_have_relations_with_all_tags() throws Exception {
        assertThat(pbfContent.getRelations()) //
                .filteredOn(relation -> relation.getTags().hasKey("name")) //
                .filteredOn(relation -> relation.getTags().hasKey("name:nl")) //
                .filteredOn(relation -> relation.getTags().hasKey("name:fr")) //
                .filteredOn(relation -> relation.getTags().hasKey("population")) //
                .filteredOn(relation -> relation.getTags().hasKey("ref:INSEE")) //
                .filteredOn(relation -> relation.getTags().hasKey("ref:tomtom")).isNotEmpty();
    }

    @Test
    public void should_have_relation_with_admin_centers_and_all_tags() throws Exception {
        verify(nameProvider).getAlternateNames(10560000718742L);

        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("admin_center")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("name")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("place")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("name:nl")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("population")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("name:fr")).isNotEmpty();
    }

    @Test
    public void should_have_relation_with_role_label_and_tag_name() throws Exception {

        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("label")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("name")).isNotEmpty();
    }

    @Test
    public void should_have_brussel_as_capital() throws Exception {

        assertThat(pbfContent.getNodes().stream())
                .filteredOn(node -> node.getTags().hasKeyValue("name", "Brussel")) //
                .filteredOn(node -> node.getTags().hasKeyValue("capital", "8")) //
                .filteredOn(node -> node.getTags().hasKeyValue("place", "city")).hasSize(1);
    }
}