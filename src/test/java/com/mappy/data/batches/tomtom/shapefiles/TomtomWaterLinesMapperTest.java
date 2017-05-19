package com.mappy.data.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.data.batches.tomtom.Tomtom2Osm;
import com.mappy.data.batches.tomtom.Tomtom2OsmModule;
import com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;

public class TomtomWaterLinesMapperTest {

    @Test
    public void should_generate_waterlines() throws Exception {
        List<Way> ways = generatePbf_and_read("fraf322").getWays();

        Optional<Way> optRiver = ways.stream().filter(way -> way.getTags().hasKeyValue("name", "La Vilaine")).findFirst();
        assertThat(optRiver.isPresent()).isTrue();
        assertThat(optRiver.get().getTags().get("waterway")).isEqualTo("river");
        assertThat(optRiver.get().getTags().get("name:fr")).isEqualTo("La Vilaine");

        Optional<Way> optStream = ways.stream().filter(way -> way.getTags().hasKeyValue("name", "Ruisseau de Kergoal")).findFirst();
        assertThat(optStream.isPresent()).isTrue();
        assertThat(optStream.get().getTags().get("waterway")).isEqualTo("stream");
        assertThat(optStream.get().getTags().get("name:fr")).isEqualTo("Ruisseau de Kergoal");
    }

    @Test
    public void should_generate_simple_waterareas() throws Exception {
        PbfContent pbfContent = generatePbf_and_read("fraf220");

        Optional<Way> optWay1 = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("name", "La Plage Bleue")).findFirst();
        assertThat(optWay1.isPresent()).isTrue();

        Tags tags1 = optWay1.get().getTags();
        assertThat(tags1.get("name:fr")).contains("La Plage Bleue");
        assertThat(tags1.get("natural")).contains("water");

    }

    private static PbfContent generatePbf_and_read(String fileName) throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", fileName)).getInstance(Tomtom2Osm.class);
        launcher.run();
        return read(new File("target/" + fileName + ".osm.pbf"));
    }
}
