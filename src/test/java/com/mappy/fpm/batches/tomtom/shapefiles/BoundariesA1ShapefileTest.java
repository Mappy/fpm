package com.mappy.fpm.batches.tomtom.shapefiles;

import com.google.inject.Guice;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class BoundariesA1ShapefileTest {

    public static Tomtom2OsmTestUtils.PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "bel")).getInstance(Tomtom2Osm.class);
        launcher.run();
        pbfContent = read(new File("target/bel.osm.pbf"));
    }

    @Test
    public void should_have_some_inner_boundaries_in_vlaams_gewest() throws Exception {
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("inner"))
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("name", "Vlaams Gewest"))
                .isNotEmpty();
    }

    @Test
    public void should_have_some_outer_boundaries_in_vlaams_gewest() throws Exception {
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("outer"))
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("name", "Vlaams Gewest"))
                .isNotEmpty();
    }


}