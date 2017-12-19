package com.mappy.fpm.batches.merge.pbf;

import com.mappy.fpm.batches.AbstractTest;
import org.junit.Test;

import java.io.File;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

public class OsmMergerTest extends AbstractTest {

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_no_input_files_are_given() throws Exception {
        new OsmMerger().merge(newArrayList(), "output");
    }

    @Test
    public void should_copy_the_only_input_file() throws Exception {
        new File("target/tests/output.osm.pbf").delete();

        new OsmMerger().merge(newArrayList("src/test/resources/merge/toll.osm.pbf"), "target/tests/output.osm.pbf");

        assertThat(new File("target/tests/output.osm.pbf").exists()).isTrue();
    }

    @Test
    public void should_merge_all_input_files_inside_output() throws Exception {
        new File("target/tests/outputAll.osm.pbf").delete();

        new OsmMerger().merge(newArrayList("src/test/resources/merge/Belgique.osm.pbf", "src/test/resources/merge/Luxembourg.osm.pbf"), "target/tests/outputAll.osm.pbf");

        assertThat(new File("target/tests/outputAll.osm.pbf").exists()).isTrue();
    }
}