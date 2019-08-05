package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.mappy.fpm.batches.tomtom.TomtomFolder;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LnDbfTest {
    private final TomtomFolder folder = mock(TomtomFolder.class);

    @Test
    public void should_parse_ln_file() {
        when(folder.getFile("ln.dbf")).thenReturn(getClass().getResource("/tomtom/ln.dbf").getPath());
        LnDbf connectivities = new LnDbf(folder);
        assertThat(connectivities.containsKey(12501009409862L)).isTrue();
        assertThat(connectivities.get(12501009409862L)).containsExactly(
            new ConnectivityInformation(12501009409862L, 1, 1, 12500004246250L),
            new ConnectivityInformation(12501009409862L, 2, 1, 12500004246250L),
            new ConnectivityInformation(12501009409862L, 3, 2, 12500004246250L),
            new ConnectivityInformation(12501009409862L, 4, 3, 12500004246250L)
        );
    }
}
