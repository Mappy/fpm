package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.google.common.collect.ArrayListMultimap;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LpDbfTest {
    private final TomtomFolder folder = mock(TomtomFolder.class);

    @Test
    public void should_parse_lp_file() {
        when(folder.getFile("lp.dbf")).thenReturn(getClass().getResource("/tomtom/lp.dbf").getPath());
        LpDbf lpDbf = new LpDbf(folder);
        ArrayListMultimap<Long, ConnectivityPath> connectivityPaths = lpDbf.getConnectivityPaths();
        assertThat(connectivityPaths.get(12501009409862L)).containsExactly(
            new ConnectivityPath(12501009409862L, 1, 12500001508968L),
            new ConnectivityPath(12501009409862L, 2, 12500001508970L)
        );
    }
}
