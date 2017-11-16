package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import net.morbz.osmonaut.osm.Way;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class WaterLineShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("wl.shp")).thenReturn("src/test/resources/tomtom/waterline/wl.shp");

        NameProvider nameProvider = mock(NameProvider.class);
        when(nameProvider.getAlternateNames(12501000193184L)).thenReturn(of("name:fr", "La Belle"));
        when(nameProvider.getAlternateNames(12502000977252L)).thenReturn(of("name:fr", "Ruisseau de Ker Iliz"));

        WaterLineShapefile shapefile = new WaterLineShapefile(tomtomFolder, nameProvider);

        verify(nameProvider).loadFromFile("wxnm.dbf", "NAME", false);

        shapefile.serialize(shapefile.getSerializer("target/tests/"));

        pbfContent = read(new File("target/tests/wl.osm.pbf"));
    }

    @Test
    public void should_generate_waterlines() throws Exception {
        List<Way> ways = pbfContent.getWays();

        Optional<Way> optRiver = ways.stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "12501000193184")).findFirst();
        assertThat(optRiver.isPresent()).isTrue();
        assertThat(optRiver.get().getTags().get("name")).isEqualTo("La Vilaine");
        assertThat(optRiver.get().getTags().get("name:fr")).isEqualTo("La Belle");
        assertThat(optRiver.get().getTags().get("waterway")).isEqualTo("river");
        assertThat(optRiver.get().getTags().get("natural")).isEqualTo("water");

        Optional<Way> optStream = ways.stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "12502000977252")).findFirst();
        assertThat(optStream.isPresent()).isTrue();
        assertThat(optStream.get().getTags().get("name")).isEqualTo("Ruisseau de Kergoal");
        assertThat(optStream.get().getTags().get("name:fr")).isEqualTo("Ruisseau de Ker Iliz");
        assertThat(optStream.get().getTags().get("waterway")).isEqualTo("stream");
        assertThat(optRiver.get().getTags().get("natural")).isEqualTo("water");
    }
}
