package com.mappy.data.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.data.batches.tomtom.Tomtom2Osm;
import com.mappy.data.batches.tomtom.Tomtom2OsmModule;
import com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;

public class LuShapefileTest {

    @Test
    public void should_change_name_to_french_when_available() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "belbe2")).getInstance(Tomtom2Osm.class);
        launcher.run();
        Tomtom2OsmTestUtils.PbfContent pbfContent = read(new File("target/belbe2.osm.pbf"));

        Optional<Way> optWay = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("name", "Universitair Kinderziekenhuis Koningin Fabiola")).findFirst();
        assertThat(optWay.isPresent()).isTrue();

        Tags tags = optWay.get().getTags();
        assertThat(tags.get("name:nl")).contains("Universitair Kinderziekenhuis Koningin Fabiola");
        assertThat(tags.get("name:fr")).contains("Hôpital Universitaire Des Enfants Reine Fabiola");
    }
}