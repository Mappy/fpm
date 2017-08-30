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

public class BoundariesA0ShapefileTest {
    public static Tomtom2OsmTestUtils.PbfContent pbfContent;

    @BeforeClass
    public static void setup() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "andandb")).getInstance(Tomtom2Osm.class);
        launcher.run();
        pbfContent = read(new File("target/andandb.osm.pbf"));
    }


    @Test
    public void should_have_relations_with_ways() throws Exception {
        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("outer")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("boundary")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKeyValue("admin_level", "2")).isNotEmpty();
    }

    @Test
    public void should_have_relations_with_all_tags() throws Exception {
        assertThat(pbfContent.getRelations()) //
                .filteredOn(relation -> relation.getTags().hasKey("name")) //
                .filteredOn(relation -> relation.getTags().hasKey("name:de")) //
                .filteredOn(relation -> relation.getTags().hasKey("name:fr")) //
                .filteredOn(relation -> relation.getTags().hasKey("name:en")) //
                .filteredOn(relation -> relation.getTags().hasKey("name:es")) //
                .filteredOn(relation -> relation.getTags().hasKey("population")) //
                .filteredOn(relation -> relation.getTags().hasKey("ref:INSEE")) //
                .filteredOn(relation -> relation.getTags().hasKey("ref:tomtom")).isNotEmpty();
    }

    @Test
    public void should_have_admin_centers_with_all_tags() throws Exception {

        assertThat(pbfContent.getRelations().stream().flatMap(relation -> relation.getMembers().stream())) //
                .filteredOn(relationMember -> relationMember.getRole().equals("label")) //
                .filteredOn(relationMember -> relationMember.getEntity().getTags().hasKey("name")).isNotEmpty();
    }

}