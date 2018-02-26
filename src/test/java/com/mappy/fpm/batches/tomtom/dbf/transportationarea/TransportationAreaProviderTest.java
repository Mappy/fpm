package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

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
    public void shoulld_have_same_id_on_both_sides(){
        Optional<String> areas = transportationAreaProvider.getAreas(123L);
        assertThat(areas).isEqualTo(of("456;456"));
    }



}