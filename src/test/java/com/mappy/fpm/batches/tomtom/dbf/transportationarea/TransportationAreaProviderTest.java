package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
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
    public void should_have_an_area_with_same_id_on_both_sides() {
        Optional<String> areas = transportationAreaProvider.getSmallestAreas(123L);
        assertThat(areas).isEqualTo(of("444"));
    }

    @Test
    public void should_not_have_ferry_elements() {
        Optional<String> areas = transportationAreaProvider.getSmallestAreas(789L);
        assertThat(areas).isEqualTo(empty());
    }

    @Test
    public void should_not_have_neighborhood_elements() {
        Optional<String> areas = transportationAreaProvider.getSmallestAreas(111L);
        assertThat(areas).isEqualTo(empty());
    }

    @Test
    public void should_have_an_area_with_different_ids_on_left_and_right() {
        Optional<String> areas = transportationAreaProvider.getSmallestAreas(222L);
        assertThat(areas).isEqualTo(of("123;789"));
    }

    @Test
    public void should_have_a_built_up_with_same_id_on_both_sides() {
        Optional<String> areas = transportationAreaProvider.getBuiltUp(123L);
        assertThat(areas).isEqualTo(of("777"));
    }
}