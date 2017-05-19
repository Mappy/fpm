package com.mappy.data.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.data.batches.tomtom.Tomtom2Osm;
import com.mappy.data.batches.tomtom.Tomtom2OsmModule;
import com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils;
import net.morbz.osmonaut.osm.Relation;
import net.morbz.osmonaut.osm.Tags;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

import static com.mappy.data.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;

public class LcShapefileTest {

    @Test
    public void should_change_name_to_french_when_available() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "belbe2")).getInstance(Tomtom2Osm.class);
        launcher.run();
        Tomtom2OsmTestUtils.PbfContent pbfContent = read(new File("target/belbe2.osm.pbf"));

        Optional<Relation> optRelation = pbfContent.getRelations().stream().filter(relation -> relation.getTags().hasKeyValue("name", "Belle-Vuebos")).findFirst();
        assertThat(optRelation.isPresent()).isTrue();

        Tags tags = optRelation.get().getTags();
        assertThat(tags.get("name:nl")).contains("Belle-Vuebos");
    }
}