package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static com.mappy.fpm.utils.MemoryFeature.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LaneTaggerTest {
    private final LaneDirections directions = mock(LaneDirections.class);
    private final LaneTagger tagger = new LaneTagger(directions);

    @Test
    public void should_parse_ld_file() throws Exception {
        when(directions.containsKey(123)).thenReturn(true);
        when(directions.get(123)).thenReturn(newArrayList("slight_left", "through", "slight_right"));

        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "TF", "LANES", "3")));

        assertThat(tags).containsEntry("turn:lanes", "slight_left|through|slight_right");
    }

    @Test
    public void should_reverse_if_needed() throws Exception {
        when(directions.containsKey(123)).thenReturn(true);
        when(directions.get(123)).thenReturn(newArrayList("slight_right", "through", "slight_left"));

        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "3")));

        assertThat(tags).containsEntry("turn:lanes", "slight_left|through|slight_right");
    }

    @Test
    public void should_add_number_of_lanes() throws Exception {
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "3")));

        assertThat(tags).containsEntry("lanes", "3");
    }
}
