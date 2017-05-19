package com.mappy.data.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.data.batches.tomtom.Tomtom2Osm;
import com.mappy.data.batches.tomtom.Tomtom2OsmModule;
import com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import org.junit.Test;

import java.io.File;

import static com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;

public class TomtomBuildingMapperTest {

    @Test
    public void should_generate_building_file() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "rennes")).getInstance(Tomtom2Osm.class);
        launcher.run();
        PbfContent pbfContent = read(new File("target/rennes.osm.pbf"));

        assertThat(pbfContent.getRelations()).hasSize(12);
        assertThat(pbfContent.getWays()).hasSize(2140);
    }
}