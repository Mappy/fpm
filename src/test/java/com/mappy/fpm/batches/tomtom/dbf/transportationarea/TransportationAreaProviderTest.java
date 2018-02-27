package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransportationAreaProviderTest {
    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);
    private TransportationAreaProvider transportationAreaProvider;


    @Before
    public void setUp() {
        when(tomtomFolder.getFile("ta.dbf")).thenReturn("src/test/resources/tomtom/transportationarea/andandand________ta.dbf");
        transportationAreaProvider = new TransportationAreaProvider(tomtomFolder);
        transportationAreaProvider.loadTransportationAreaAttributes("ta.dbf");
    }

    @Test
    public void should_have_same_id_on_both_sides() {
        String areas = transportationAreaProvider.getAreas(123L);
        assertThat(areas).isEqualTo("456");
    }

    @Test
    public void should_not_have_ferry_elements() {
        String areas = transportationAreaProvider.getAreas(789L);
        assertThat(areas).isEqualTo("");
    }

    @Test
    public void should_not_have_neighborhood_elements() {
        String areas = transportationAreaProvider.getAreas(111L);
        assertThat(areas).isEqualTo("");
    }

    @Test
    public void should_have_different_ids_on_left_and_right() {
        String areas = transportationAreaProvider.getAreas(222L);
        assertThat(areas).isEqualTo("123;789");
    }


}