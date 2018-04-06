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
    }


    @Test
    public void should_not_have_ferry_elements() {
        Optional<String> areas = transportationAreaProvider.getLeftSmallestAreas(789L);
        assertThat(areas).isEqualTo(empty());
    }

    @Test
    public void should_not_have_neighborhood_elements() {
        Optional<String> areas = transportationAreaProvider.getRightSmallestAreas(111L);
        assertThat(areas).isEqualTo(empty());
    }

    @Test
    public void should_have_an_area_with_different_ids_on_left() {
        Optional<String> areasLeft = transportationAreaProvider.getLeftSmallestAreas(222L);
        assertThat(areasLeft).isEqualTo(of("123"));
    }

    @Test
    public void should_have_an_area_with_different_ids_on_right() {
        Optional<String> areasRight = transportationAreaProvider.getRightSmallestAreas(222L);
        assertThat(areasRight).isEqualTo(of("789"));
    }

    @Test
    public void should_have_a_built_up_without_left_sides() {
        Optional<String> areasLeft = transportationAreaProvider.getBuiltUpLeft(123L);
        Optional<String> areasRight = transportationAreaProvider.getBuiltUpRight(123L);
        assertThat(areasRight).isEqualTo(areasLeft).contains("777");
    }

    @Test
    public void should_have_same_ids_on_left_and_right() {
        Optional<String> areasLeft = transportationAreaProvider.getLeftSmallestAreas(123L);
        Optional<String> areasRight = transportationAreaProvider.getRightSmallestAreas(123L);
        assertThat(areasLeft).isEqualTo(areasRight).contains("444");
    }
}