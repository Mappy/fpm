package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LuShapefileTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("lu.shp")).thenReturn("src/test/resources/tomtom/landuse/bel_lu.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(anyLong())).thenReturn(ImmutableMap.of("name:nl", "Universitair Kinderziekenhuis", "name:fr", "Hôpital Universitaire Des Enfants"));

        LuShapefile shapefile = new LuShapefile(tomtomFolder, nameProvider);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/bel_lu.osm.pbf", "Test_TU");

        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/bel_lu.osm.pbf"));
    }

    @Test
    public void should_change_name_to_french_when_available() throws Exception {
        Optional<Way> optWay = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("name", "Universitair Kinderziekenhuis Koningin Fabiola")).findFirst();
        assertThat(optWay.isPresent()).isTrue();

        Tags tags = optWay.get().getTags();
        assertThat(tags.get("name:nl")).contains("Universitair Kinderziekenhuis");
        assertThat(tags.get("name:fr")).contains("Hôpital Universitaire Des Enfants");
    }
}