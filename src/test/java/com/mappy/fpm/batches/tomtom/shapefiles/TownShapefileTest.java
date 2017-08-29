package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
import com.mappy.fpm.batches.utils.OsmosisSerializer;
import net.morbz.osmonaut.osm.Node;
import net.morbz.osmonaut.osm.RelationMember;
import org.junit.Test;

import javax.annotation.meta.When;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TownShapefileTest {



    @Test
    public void should_change_name_to_french_when_available() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "belbe2")).getInstance(Tomtom2Osm.class);
        launcher.run();
        Tomtom2OsmTestUtils.PbfContent pbfContent = read(new File("target/belbe2.osm.pbf"));

        assertThat(pbfContent.getNodes())
                .filteredOn(node -> node.getTags().hasKeyValue("name", "Brussel"))
                .filteredOn(node -> node.getTags().hasKeyValue("name:nl", "Brussel"))
                .filteredOn(node -> node.getTags().hasKeyValue("name:fr", "Bruxelles"))
                .hasSize(1);
    }


    @Test
    public void should_have_tag_population_and_relations() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "belbe3")).getInstance(Tomtom2Osm.class);
        launcher.run();
        Tomtom2OsmTestUtils.PbfContent pbfContent = read(new File("target/belbe3.osm.pbf"));

        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream()).filter( t -> t.getEntity().getTags().hasKey("population")).count()).isEqualTo(2);
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream()).filter(relationMember -> relationMember.getRole().equals("admin_center")).count()).isEqualTo(2);
        assertThat(pbfContent.getRelations().stream().filter(relation -> relation.getTags().hasKey("population")).findFirst()).isNotEmpty();
    }



}