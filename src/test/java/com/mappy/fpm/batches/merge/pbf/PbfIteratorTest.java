package com.mappy.fpm.batches.merge.pbf;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PbfIteratorTest {

    @Test(expected = RuntimeException.class)
    public void should_throw_runtimeException_when_file_not_found() {
        new PbfIterator("doesntExistsFile.osm.pbf");
    }

    @Test(timeout = 1000)
    public void should_iter_over_pbf_entities() {
        PbfIterator pbfIterator = new PbfIterator("src/test/resources/merge/toll.osm.pbf");

        assertThat(pbfIterator).hasSize(6);
    }

    @Test
    public void can_call_hasNext_several_times() {
        PbfIterator pbfIterator = new PbfIterator("src/test/resources/merge/toll.osm.pbf");

        assertThat(pbfIterator.hasNext()).isTrue();
        assertThat(pbfIterator.hasNext()).isTrue();
        assertThat(pbfIterator.hasNext()).isTrue();
        assertThat(pbfIterator.hasNext()).isTrue();
        assertThat(pbfIterator.hasNext()).isTrue();
        assertThat(pbfIterator.hasNext()).isTrue();
    }

    @Test
    public void can_call_next_whithout_hasNext() {
        PbfIterator pbfIterator = new PbfIterator("src/test/resources/merge/toll.osm.pbf");

        assertThat(pbfIterator.next()).isNotNull();
        assertThat(pbfIterator.next()).isNotNull();
        assertThat(pbfIterator.next()).isNotNull();
        assertThat(pbfIterator.next()).isNotNull();
        assertThat(pbfIterator.next()).isNotNull();
        assertThat(pbfIterator.next()).isNotNull();

        assertThat(pbfIterator.hasNext()).isFalse();
    }
}
