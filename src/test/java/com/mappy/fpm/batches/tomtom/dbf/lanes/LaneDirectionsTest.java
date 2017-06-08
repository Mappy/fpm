package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.mappy.fpm.batches.tomtom.TomtomFolder;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LaneDirectionsTest {
    private final TomtomFolder folder = mock(TomtomFolder.class);

    @Test
    public void should_parse_ld_file() throws Exception {
        when(folder.getFile("ld.dbf")).thenReturn(getClass().getResource("/tomtom/ld.dbf").getPath());
        LaneDirections directions = new LaneDirections(folder);

        assertThat(directions.containsKey(12500001779461L)).isTrue();
        assertThat(directions.containsKey(123L)).isFalse();

        assertThat(directions.get(12500001779461L)).containsExactly("through;right", "through;right", "through", "through;left");
        assertThat(directions.get(12500001777619L)).containsExactly("slight_left", "through", "slight_right");
        assertThat(directions.get(12500001785869L)).containsExactly("through");
        assertThat(directions.get(12500001779787L)).containsExactly("through", "left", "left", "right");
    }
}
