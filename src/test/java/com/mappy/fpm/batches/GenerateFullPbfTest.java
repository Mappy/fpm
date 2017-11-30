package com.mappy.fpm.batches;

import com.mappy.fpm.batches.merge.pbf.OsmMerger;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.PbfContent;
import net.morbz.osmonaut.osm.RelationMember;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.tomtom.Tomtom2OsmTestUtils.read;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class GenerateFullPbfTest {

    private static PbfContent pbfContent;

    private final OsmMerger osmMerger = Mockito.spy(new OsmMerger());
    private final GenerateFullPbf generateFullPbf = new GenerateFullPbf(osmMerger, "src/test/resources/generateFullPbf", "target", "Europe.osm.pbf", 1);

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_when_country_is_unknown() throws Exception {
        generateFullPbf.run(newArrayList("fakeCountry"));
    }

    @Test
    public void should_generate_Andorra() throws Exception {

        generateFullPbf.run(newArrayList("Andorre"));

        assertThat(new File("target/Andorre/pbfFiles/and.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Andorre/pbfFiles/andand.osm.pbf").exists()).isTrue();
        assertThat(new File("target/Andorre/Andorre.osm.pbf").exists()).isTrue();

        verify(osmMerger).merge(anyListOf(String.class), eq("target/Andorre/Andorre.osm.pbf"));
        verify(osmMerger).merge(anyListOf(String.class), eq("target/Europe.osm.pbf"));

        pbfContent = read(new File("target/Europe.osm.pbf"));

        List<RelationMember> outer = pbfContent.getRelations().stream()
                .filter(r -> r.getTags().hasKeyValue("ref:tomtom", "10200000000008"))
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> relationMember.getRole().equals("outer")) //
                .collect(toList());
        assertThat(outer).isNotEmpty();

        List<RelationMember> label = pbfContent.getRelations().stream()
                .filter(r -> r.getTags().hasKeyValue("ref:tomtom", "10200000000008"))
                .flatMap(relation -> relation.getMembers().stream())//
                .filter(relationMember -> relationMember.getRole().equals("label")) //
                .collect(toList());
        assertThat(label).isNotEmpty();
    }
}