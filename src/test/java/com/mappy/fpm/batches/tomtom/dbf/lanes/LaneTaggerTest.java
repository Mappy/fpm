package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.utils.MemoryFeature.onlyTags;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LaneTaggerTest {
    private final LdDbf directions = mock(LdDbf.class);
    private final LfDbf trafficFlow = mock(LfDbf.class);
    private final LaneTagger tagger = new LaneTagger(directions, trafficFlow);

    @Test
    public void should_parse_ld_file() {
        when(directions.containsKey(123)).thenReturn(true);
        when(directions.get(123)).thenReturn(newArrayList("slight_left", "through", "slight_right"));

        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "TF", "LANES", "3")), true);

        assertThat(tags).containsEntry("turn:lanes", "slight_left|through|slight_right");
    }

    @Test
    public void should_reverse_if_needed() {
        when(directions.containsKey(123)).thenReturn(true);
        when(directions.get(123)).thenReturn(newArrayList("slight_right", "through", "slight_left"));

        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "3")), false);

        assertThat(tags).containsEntry("turn:lanes", "slight_left|through|slight_right");
    }

    @Test
    public void should_add_number_of_lanes() {
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "3")), false);

        assertThat(tags).containsEntry("lanes", "3");
    }
}
