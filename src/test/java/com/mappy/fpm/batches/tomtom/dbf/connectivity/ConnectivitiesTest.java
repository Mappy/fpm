package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.google.common.collect.ArrayListMultimap;

import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConnectivitiesTest {
    private final LnDbf ln = mock(LnDbf.class);
    private final LpDbf lp = mock(LpDbf.class);

    @Test
    public void should_generate_connectivity() {
        ArrayListMultimap<Long, ConnectivityPath> samplePathList = ArrayListMultimap.create();
        samplePathList.put(123L, new ConnectivityPath(123L, 1, 12501L));
        samplePathList.put(123L, new ConnectivityPath(123L, 2, 12502L));
        when(lp.getConnectivityPaths()).thenReturn(samplePathList);
        when(ln.containsKey(123L)).thenReturn(true);
        when(ln.get(123L)).thenReturn(newArrayList(
            new ConnectivityInformation(123L, 1, 1, 125999L),
            new ConnectivityInformation(123L, 1, 2, 125999L),
            new ConnectivityInformation(123L, 2, 3, 125999L)
        ));

        Connectivities connectivities = new Connectivities(ln, lp);

        ArrayListMultimap<Integer, Integer> expectedLaneMapping = ArrayListMultimap.create();
        expectedLaneMapping.put(1, 1);
        expectedLaneMapping.put(1, 2);
        expectedLaneMapping.put(2, 3);
        assertThat(connectivities.getConnectivitiesStartingAtSection(12501)).containsExactly(
            new Connectivity(123L, 125999L, 12501L, newArrayList(12502L), expectedLaneMapping)
        );
    }

    @Test
    public void should_support_long_path_sequence() {
        ArrayListMultimap<Long, ConnectivityPath> samplePathList = ArrayListMultimap.create();
        samplePathList.put(123L, new ConnectivityPath(123L, 1, 12501L));
        samplePathList.put(123L, new ConnectivityPath(123L, 2, 12502L));
        samplePathList.put(123L, new ConnectivityPath(123L, 3, 12503L));
        when(lp.getConnectivityPaths()).thenReturn(samplePathList);
        when(ln.containsKey(123L)).thenReturn(true);
        when(ln.get(123L)).thenReturn(newArrayList(
            new ConnectivityInformation(123L, 1, 1, 125999L)
        ));

        Connectivities connectivities = new Connectivities(ln, lp);

        ArrayListMultimap<Integer, Integer> expectedLaneMapping = ArrayListMultimap.create();
        expectedLaneMapping.put(1, 1);
        assertThat(connectivities.getConnectivitiesStartingAtSection(12501)).containsExactly(
            new Connectivity(123L, 125999L, 12501L, newArrayList(12502L, 12503L), expectedLaneMapping)
        );
    }
}
