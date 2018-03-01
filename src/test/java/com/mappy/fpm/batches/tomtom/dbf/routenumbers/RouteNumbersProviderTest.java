package com.mappy.fpm.batches.tomtom.dbf.routenumbers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouteNumbersProviderTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);
    private RouteNumbersProvider routeNumbers;

    @Before
    public void setUp() {
        when(tomtomFolder.getFile("rn.dbf")).thenReturn("src/test/resources/tomtom/routenumbers/luxlux___________rn.dbf");
        routeNumbers = new RouteNumbersProvider(tomtomFolder);
        routeNumbers.loadGeocodingAttributes("rn.dbf");

    }


    @Test
    public void should_add_alternative_road_names() {
        String tag = routeNumbers.getRouteNumbers(123L);
        assertThat(tag).isEmpty();
    }

}