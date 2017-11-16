package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
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
        when(tomtomFolder.getFile("2dtb.shp")).thenReturn("src/test/resources/tomtom/landuse/2dtb.shp");

        HighQualityLanduseShapefile shapefile = new HighQualityLanduseShapefile(tomtomFolder);

        shapefile.serialize(shapefile.getSerializer("target/tests/"));

        pbfContent = read(new File("target/tests/2dtb.osm.pbf"));
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