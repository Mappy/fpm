package com.mappy.data.batches.tomtom.shapefiles;

import com.mappy.data.batches.tomtom.Tomtom2Osm;
import com.mappy.data.batches.tomtom.Tomtom2OsmModule;
import com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils;
import com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import net.morbz.osmonaut.osm.Node;
import org.junit.Test;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

import static com.google.inject.Guice.createInjector;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class TomtomTownMapperTest {

    @Test
    public void should_read_cities_labels() throws Exception {

        Tomtom2Osm launcher = createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "fraf622")).getInstance(Tomtom2Osm.class);
        launcher.run();
        PbfContent pbfContent = Tomtom2OsmTestUtils.read(new File("target/fraf622.osm.pbf"));

        Map<Long, Node> nodesById = pbfContent.getNodes().stream().collect(uniqueIndexBy(Node::getId));

        Node nodeCity = nodesById.get(22692895375609124L);
        assertThat(nodeCity.getTags().get("name")).isEqualTo("Rennes");
        assertThat(nodeCity.getTags().get("place")).isEqualTo("city");

        Node nodeVillage = nodesById.get(22692892068274045L);
        assertThat(nodeVillage.getTags().get("name")).isEqualTo("Saint-Grégoire");
        assertThat(nodeVillage.getTags().get("place")).isEqualTo("village");
    }

    private static <T, K> Collector<T, ?, Map<K, T>> uniqueIndexBy(Function<? super T, ? extends K> keyMapper) {
        return toMap(keyMapper, t -> t, (a, b) -> {
            throw new IllegalArgumentException("Duplicate key: " + a);
        });
    }
}