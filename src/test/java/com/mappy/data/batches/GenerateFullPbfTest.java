package com.mappy.data.batches;

import com.mappy.data.batches.merge.pbf.OsmMerger;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class GenerateFullPbfTest {
    private final OsmMerger osmMerger = Mockito.spy(new OsmMerger());
    private final GenerateFullPbf generateFullPbf = new GenerateFullPbf(osmMerger, "src/test/resources/generateFullPbf", "target", "Europe.osm.pbf", 1);

    @Test
    public void should_generate_luxembourg() throws Exception {
        File belgique = new File("target/Belgique/Belgique.osm.pbf");
        belgique.delete();
        File luxembourg = new File("target/Luxembourg/Luxembourg.osm.pbf");
        luxembourg.delete();
        File europe = new File("target/Europe.osm.pbf");
        europe.delete();

        generateFullPbf.run(newArrayList("Belgique", "Luxembourg"));

        assertThat(new File("target/Belgique/pbfFiles/bel.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Belgique/pbfFiles/belbe2.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Belgique/pbfFiles/brussels.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Luxembourg/pbfFiles/lux.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Luxembourg/pbfFiles/luxembourg.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Luxembourg/pbfFiles/luxlux.osm.pbf").exists()).isTrue();

        verify(osmMerger).merge(anyListOf(String.class), eq("target/Belgique/Belgique.osm.pbf"));
        verify(osmMerger).merge(anyListOf(String.class), eq("target/Luxembourg/Luxembourg.osm.pbf"));
        assertThat(belgique.exists()).isTrue();
        assertThat(luxembourg.exists()).isTrue();

        verify(osmMerger).merge(anyListOf(String.class), eq("target/Europe.osm.pbf"));
        assertThat(europe.exists()).isTrue();
    }
}