package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import org.junit.Test;

import java.io.File;

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;

public class TomtomLandsMapperTest {

    @Test
    public void should_generate_landuseandcover_file() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "fraf122")).getInstance(Tomtom2Osm.class);
        launcher.run();
        PbfContent pbfContent = read(new File("target/fraf122.osm.pbf"));

        assertThat(pbfContent.getWays())
                .filteredOn(w -> w.getTags().hasKeyValue("name", "Hippodrome Maure de Bretagne"))
                .filteredOn(w -> w.getTags().hasKeyValue("leisure", "stadium"))
                .hasSize(1);

        assertThat(pbfContent.getWays())
                .filteredOn(w -> w.getTags().hasKeyValue("feattyp", "7110"))
                .isEmpty();

        assertThat(pbfContent.getWays()).filteredOn(w -> w.getTags().hasKeyValue("landuse", "forest")).isNotEmpty();
        assertThat(pbfContent.getWays()).filteredOn(w -> w.getTags().hasKeyValue("natural", "grassland")).isNotEmpty();
        assertThat(pbfContent.getWays()).filteredOn(w -> w.getTags().hasKeyValue("landuse", "industrial")).isNotEmpty();
    }
}