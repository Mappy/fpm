package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoundariesA0ShapefileTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {
        createDirectory(get("target", "tests"));

        NameProvider nameProvider = mock(NameProvider.class);
        Map<String, String> names = newHashMap();
        names.putAll(of("name", "Andorra", "name:fr", "Andorre"));
        when(nameProvider.getAlternateNames(10200000000008L)).thenReturn(names);

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("___a0.shp")).thenReturn("src/test/resources/tomtom/boundaries/a0/andorra______________a0.shp");
        BoundariesA0Shapefile shapefile = new BoundariesA0Shapefile(tomtomFolder, nameProvider);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/andorra.osm.pbf", "Test_TU");

        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/andorra.osm.pbf"));
    }

    @Test
    public void should_have_relations_with_ways() throws Exception {
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("outer")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("boundary")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("name")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("admin_level", "2")).isNotEmpty();
    }

    @Test
    public void should_have_relations_with_all_tags() throws Exception {
        assertThat(pbfContent.getRelations()) //
                .filteredOn(relation -> relation.getTags().hasKeyValue("name", "Andorra")) //
                .filteredOn(relation -> relation.getTags().hasKeyValue("name:fr", "Andorre")) //
                .filteredOn(relation -> relation.getTags().hasKeyValue("ref:INSEE", "20")) //
                .filteredOn(relation -> relation.getTags().hasKeyValue("ref:tomtom", "10200000000008")).isNotEmpty();
    }

    @Test
    public void should_have_relation_with_role_label_and_tags() throws Exception {
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("label")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("name", "Andorra")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("name:fr", "Andorre")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("ref:INSEE", "20")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("ref:tomtom", "10200000000008")).isNotEmpty();
    }

    @Test
    public void should_not_have_a_null_or_empty_population_on_relation() throws Exception {
        assertThat(pbfContent.getRelations()).filteredOn(relation -> relation.getTags().hasKey("population")).isEmpty();
    }
}
