package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.AbstractTest;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import net.morbz.osmonaut.osm.Way;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RailwayShapefileTest extends AbstractTest {

    private static PbfContent pbfContent;

    @BeforeClass
    public static void loadPbf() throws Exception {

        TomtomFolder tomtomFolder = mock(TomtomFolder.class);
        when(tomtomFolder.getFile("rr.shp")).thenReturn("src/test/resources/tomtom/railway/rr.shp");

        RailwayShapefile shapefile = new RailwayShapefile(tomtomFolder);

        shapefile.serialize(shapefile.getSerializer("target/tests/"));

        pbfContent = read(new File("target/tests/rr.osm.pbf"));
    }

    @Test
    public void should_read_railway() {
        Optional<Way> optWay = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "17561000011991")).findFirst();
        assertThat(optWay.isPresent()).isTrue();
        assertThat(optWay.get().getTags().get("railway")).contains("rail");
        assertThat(optWay.get().getTags().get("tunnel")).isNull();
        assertThat(optWay.get().getTags().get("bridge")).isNull();
    }

    @Test
    public void should_read_railway_in_tunnel() {
        Optional<Way> optWay = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "17561000014763")).findFirst();
        assertThat(optWay.isPresent()).isTrue();
        assertThat(optWay.get().getTags().get("railway")).contains("rail");
        assertThat(optWay.get().getTags().get("tunnel")).contains("yes");
        assertThat(optWay.get().getTags().get("bridge")).isNull();
    }

    @Test
    public void should_read_railway_on_bridge() {
        Optional<Way> optWay = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("ref:tomtom", "17561000013917")).findFirst();
        assertThat(optWay.isPresent()).isTrue();
        assertThat(optWay.get().getTags().get("railway")).contains("rail");
        assertThat(optWay.get().getTags().get("tunnel")).isNull();
        assertThat(optWay.get().getTags().get("bridge")).contains("yes");
    }
}