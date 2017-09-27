package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.RelationProvider;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
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
import static org.mockito.Mockito.when;

public class BoundariesA8ShapefileTest {
    public static Tomtom2OsmTestUtils.PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {
        Path dir = get("target", "tests");
        if(!dir.toFile().exists()) {
            createDirectory(dir);
        }

        NameProvider nameProvider = mock(NameProvider.class);
        Map<String, String> names = newHashMap();
        names.putAll(of("name", "Brussel", "name:fr", "Bruxelles"));
        when(nameProvider.getAlternateNames(10560000000843L)).thenReturn(names);

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a8.shp")).thenReturn("src/test/resources/tomtom/boundaries/a8/belbe3___________a8.shp");

        OsmLevelGenerator osmLevelGenerator = mock(OsmLevelGenerator.class);
        when(osmLevelGenerator.getOsmLevel("belbe3", "8")).thenReturn("8");

        RelationProvider relationProvider = mock(RelationProvider.class);

        BoundariesA8Shapefile shapefile = new BoundariesA8Shapefile(tomtomFolder, nameProvider, osmLevelGenerator, new RelationProvider());

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
                .filteredOn(node -> node.getTags().hasKeyValue("capital", "yes")) //
                .filteredOn(node -> node.getTags().hasKeyValue("place", "city")).hasSize(1);
    }

    @Test
    public void should_have_some_brussel_neighbourhood() throws Exception {

        assertThat(pbfContent.getNodes().stream())
                .filteredOn(node -> node.getTags().hasKeyValue("place", "neighbourhood")).isNotEmpty();
    }

}