package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class WaterAreaShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("wa.shp")).thenReturn("src/test/resources/tomtom/waterarea/wa.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(12502000106435L)).thenReturn(of("name:fr", "La Plage Rouge"));

        WaterAreaShapefile shapefile = new WaterAreaShapefile(tomtomFolder, nameProvider);

        verify(nameProvider).loadFromFile("wxnm.dbf", "NAME", false);

        shapefile.serialize("target/tests/");

        pbfContent = read(new File("target/tests/wa.osm.pbf"));
    }

    @Test
    public void should_generate_simple_waterareas() throws Exception {

        Optional<Way> optWay1 = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "12502000106435")).findFirst();
        assertThat(optWay1.isPresent()).isTrue();

        Tags tags1 = optWay1.get().getTags();
        assertThat(tags1.get("natural")).contains("water");
        assertThat(tags1.get("name")).contains("La Plage Bleue");
        assertThat(tags1.get("name:fr")).contains("La Plage Rouge");
    }
}