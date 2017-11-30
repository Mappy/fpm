package com.mappy.fpm.batches;

import com.mappy.fpm.batches.merge.pbf.OsmMerger;
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

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_when_country_is_unknown() throws Exception {
        generateFullPbf.run(newArrayList("fakeCountry"));
    }

    @Test
    public void should_generate_Andorre() throws Exception {
        File Andorre = new File("target/Andorre/Andorre.osm.pbf");
        Andorre.delete();
        File europe = new File("target/Europe.osm.pbf");
        europe.delete();

        generateFullPbf.run(newArrayList("Andorre"));

        assertThat(new File("target/Andorre/pbfFiles/and.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Andorre/pbfFiles/andand.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Andorre/Andorre.osm.pbf").exists()).isTrue();

        verify(osmMerger).merge(anyListOf(String.class), eq("target/Andorre/Andorre.osm.pbf"));
        assertThat(Andorre.exists()).isTrue();

        verify(osmMerger).merge(anyListOf(String.class), eq("target/Europe.osm.pbf"));
        assertThat(europe.exists()).isTrue();
    }
}