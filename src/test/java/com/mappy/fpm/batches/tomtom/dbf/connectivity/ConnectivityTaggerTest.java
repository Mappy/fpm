package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.utils.MemoryFeature.onlyTags;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectivityTaggerTest {
    private final Connectivities connectivities = mock(Connectivities.class);
    private final ConnectivityTagger connectivityTagger = new ConnectivityTagger(connectivities);

    @Test
    public void should_handle_sections_with_no_connectivities() {
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(null);
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "3",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsExactly();
    }

    @Test
    public void should_handle_oneway_connectivity() {
        ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
        laneMapping.put(1, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125999L, 123L, newArrayList(131L), laneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "1",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:forward:tomtom", "131");
    }

    @Test
    public void should_handle_sequence_of_destination_sections() {
        ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
        laneMapping.put(1, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125999L, 123L, newArrayList(131L, 132L), laneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "1",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:forward:tomtom", "131_132");
    }

    @Test
    public void should_handle_several_destinations_for_a_single_lane() {
        ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
        laneMapping.put(1, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125999L, 123L, newArrayList(131L), laneMapping),
            new Connectivity(2L, 125999L, 123L, newArrayList(132L), laneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "1",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:forward:tomtom", "131;132");
    }

    @Test
    public void should_handle_several_lanes_for_same_destination() {
        ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
        laneMapping.put(1, 1);
        laneMapping.put(2, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125999L, 123L, newArrayList(131L), laneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "2",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:forward:tomtom", "131|131");
    }

    @Test
    public void should_handle_several_lanes_with_different_destinations() {
        ArrayListMultimap<Integer, Integer> firstLaneMapping = ArrayListMultimap.create();
        firstLaneMapping.put(1, 1);
        ArrayListMultimap<Integer, Integer> secondLaneMapping = ArrayListMultimap.create();
        secondLaneMapping.put(2, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125999L, 123L, newArrayList(131L), firstLaneMapping),
            new Connectivity(2L, 125999L, 123L, newArrayList(132L), secondLaneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "2",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:forward:tomtom", "132|131");
    }

    @Test
    public void should_handle_connectivity_backward() {
        ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
        laneMapping.put(1, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125001, 123L, newArrayList(131L), laneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "1",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:backward:tomtom", "131");
    }

    @Test
    public void should_handle_twoway_roads_with_connectivity_forward() {
        ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
        laneMapping.put(1, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125999, 123L, newArrayList(131L), laneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "2",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:forward:tomtom", "|131");
    }

    @Test
    public void should_handle_twoway_roads_with_connectivity_backward() {
        ArrayListMultimap<Integer, Integer> laneMapping = ArrayListMultimap.create();
        laneMapping.put(2, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125001, 123L, newArrayList(131L), laneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "2",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:backward:tomtom", "131|");
    }

    @Test
    public void should_handle_twoway_roads_with_connectivity_at_both_ends() {
        ArrayListMultimap<Integer, Integer> fromLaneMapping = ArrayListMultimap.create();
        fromLaneMapping.put(2, 1);
        ArrayListMultimap<Integer, Integer> toLaneMapping = ArrayListMultimap.create();
        toLaneMapping.put(1, 1);
        when(connectivities.getConnectivitiesStartingAtSection(123L)).thenReturn(newArrayList(
            new Connectivity(1L, 125001L, 123L, newArrayList(131L), fromLaneMapping),
            new Connectivity(2L, 125999L, 123L, newArrayList(132L), toLaneMapping)
        ));
        Map<String, String> tags = connectivityTagger.tagConnectivities(onlyTags(ImmutableMap.of(
            "ID", "123",
            "LANES", "2",
            "F_JNCTID", "125001",
            "T_JNCTID", "125999"
        )));
        assertThat(tags).containsEntry("lanes:connectivity:forward:tomtom", "|132");
        assertThat(tags).containsEntry("lanes:connectivity:backward:tomtom", "131|");
    }
}
