package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
import org.junit.Test;

import java.io.File;

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;

public class BuiltUpShapefileTest {

    @Test
    public void should_have_some_built_up_area() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "andand")).getInstance(Tomtom2Osm.class);
        launcher.run();
        Tomtom2OsmTestUtils.PbfContent pbfContent = read(new File("target/andand.osm.pbf"));

        assertThat(pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("landuse", "residential")).findFirst()).isNotEmpty();

    }

}