package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Stream.of;
import static org.assertj.core.api.Assertions.assertThat;

public class BoundariesA0ShapefileTest {

    @Test
    public void should_generate_boundaries_with_admin_level_and_tomtom_ref() throws Exception {
        Tomtom2Osm launcher = createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "andandb")).getInstance(Tomtom2Osm.class);
        launcher.run();
        Tomtom2OsmTestUtils.PbfContent pbfContent = read(new File("target/andandb.osm.pbf"));

        Optional<Way> wayOptional = pbfContent.getWays().stream().filter(way -> way.getTags().hasKeyValue("boundary", "administrative")).findFirst();
        assertThat(wayOptional.isPresent()).isTrue();
        Tags tags = wayOptional.get().getTags();
        assertThat(tags.get("ref:tomtom")).isEqualTo("10200000000008");
        assertThat(tags.get("admin_level")).isEqualTo("2");
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream()).filter(relationMember -> relationMember.getRole().equals("admin_center")).count()).isEqualTo(1);
        assertThat(of("name:fr", "name:de", "name:en", "name:ca", "name:es").allMatch(tags::hasKey)).isTrue();

    }

}