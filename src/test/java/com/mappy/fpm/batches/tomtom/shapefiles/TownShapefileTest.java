package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.google.inject.Guice.createInjector;
import static org.assertj.core.api.Assertions.assertThat;

public class TownShapefileTest {
    public static PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {
        Tomtom2Osm launcher = createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "fraf622")).getInstance(Tomtom2Osm.class);
        launcher.run();
        pbfContent = Tomtom2OsmTestUtils.read(new File("target/fraf622.osm.pbf"));
    }

    @Test
    public void should_have_rennes_capital_of_britain_and_big_city() {
        assertThat(pbfContent.getNodes().stream())
                .filteredOn(node -> node.getTags().hasKeyValue("name", "Rennes"))
                .filteredOn(node -> node.getTags().hasKeyValue("place", "city"))
                .filteredOn(node -> node.getTags().hasKeyValue("capital", "1")).isNotEmpty();
    }

    @Test
    public void should_have_saint_gregoire_town() {
        assertThat(pbfContent.getNodes().stream())
                .filteredOn(node -> node.getTags().hasKeyValue("name", "Saint-Grégoire"))
                .filteredOn(node -> node.getTags().hasKeyValue("place", "town")).isNotEmpty();

    }

    @Test
    public void should_have_la_lisiere_village() {
        assertThat(pbfContent.getNodes().stream())
                .filteredOn(node -> node.getTags().hasKeyValue("name", "La Lisière"))
                .filteredOn(node -> node.getTags().hasKeyValue("place", "village")).isNotEmpty();

    }
}