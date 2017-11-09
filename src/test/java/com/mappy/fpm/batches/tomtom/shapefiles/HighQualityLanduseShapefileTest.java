package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
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

public class HighQualityLanduseShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("2dtb.shp")).thenReturn("src/test/resources/tomtom/landuse/paris_2dtb.shp");

        HighQualityLanduseShapefile shapefile = new HighQualityLanduseShapefile(tomtomFolder);

        GeometrySerializer serializer = new OsmosisSerializer("target/tests/paris.osm.pbf", "Test_TU");

        shapefile.serialize(serializer);
        serializer.close();

        pbfContent = read(new File("target/tests/paris.osm.pbf"));
    }

    @Test
    public void should_have_grass() {
        List<Tags> tags = pbfContent.getRelations().stream()
                .map(Entity::getTags)
                .collect(toList());

        assertThat(tags).extracting(t -> t.get("landuse")).containsOnly("grass");
        assertThat(tags).extracting(t -> t.get("source")).containsOnly("Tomtom - Citymap");
    }
}