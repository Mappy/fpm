package com.mappy.data.batches.merge.pbf;

import org.junit.Test;

import java.io.File;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class OsmMergerTest {

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_no_input_files_are_given() throws Exception {
        new OsmMerger().merge(newArrayList(), "output");
    }

    @Test
    public void should_copy_the_only_input_file() throws Exception {
        new File("target/output.osm.pbf").delete();

        new OsmMerger().merge(newArrayList("src/test/resources/merge/toll.osm.pbf"), "target/output.osm.pbf");

        assertThat(new File("target/output.osm.pbf").exists()).isTrue();
    }

    @Test
    public void should_merge_all_input_files_inside_output() throws Exception {
        new File("target/outputAll.osm.pbf").delete();

        new OsmMerger().merge(newArrayList("src/test/resources/merge/Belgique.osm.pbf", "src/test/resources/merge/Luxembourg.osm.pbf"), "target/outputAll.osm.pbf");

        assertThat(new File("target/outputAll.osm.pbf").exists()).isTrue();
    }
}