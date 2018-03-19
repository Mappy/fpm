package com.mappy.fpm.batches.tomtom.dbf.routenumbers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

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
    }

    @Test
    public void should_add_international_road_name() {
        Optional<String> tag = routeNumbers.getInternationalRouteNumbers(123L);
        assertThat(tag).isEqualTo(Optional.of("E41"));
    }

    @Test
    public void should_add_national_road_name() {
        Optional<String> tag = routeNumbers.getNationalRouteNumbers(123L);
        assertThat(tag).isEqualTo(Optional.of("N5"));
    }

}