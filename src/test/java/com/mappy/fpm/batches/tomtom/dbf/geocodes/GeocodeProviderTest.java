package com.mappy.fpm.batches.tomtom.dbf.geocodes;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeocodeProviderTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);
    private GeocodeProvider geocodeProvider;

    @Before
    public void setUp() {
        when(tomtomFolder.getFile("gc.dbf")).thenReturn("src/test/resources/tomtom/geocode/andand___________gc.dbf");
        geocodeProvider = new GeocodeProvider(tomtomFolder);
        geocodeProvider.loadGeocodingAttributes("gc.dbf");

    }

    @Test
    public void should_add_alternative_road_names() {
        Map<String, String> tags = geocodeProvider.getAlternateRoadNamesWithSide(10200000002470L);
        assertThat(tags).hasSize(1);
        assertThat(tags.get("name:ca")).isEqualTo("Avinguda del Consell de la Terra");
    }

    @Test
    public void should_add_alternative_road_names_on_left() {
        Map<String, String> tags = geocodeProvider.getAlternateRoadNamesWithSide(10200000000143L);
        assertThat(tags).hasSize(1);
        assertThat(tags.get("name:left:ca")).isEqualTo("Avinguda Carlemany");
    }

    @Test
    public void should_add_alternative_road_names_on_right() {
        Map<String, String> tags = geocodeProvider.getAlternateRoadNamesWithSide(10200000000176L);
        assertThat(tags).hasSize(1);
        assertThat(tags.get("name:right:ca")).isEqualTo("Carrer d'Isabelle Sandy");
    }

    @Test
    public void should_add_alternative_road_names_in_french() {
        Map<String, String> tags = geocodeProvider.getAlternateRoadNamesWithSide(10200000003341L);
        assertThat(tags).hasSize(2);
        assertThat(tags.get("name:ca")).isEqualTo("Carrer de La Unió");
        assertThat(tags.get("name:fr")).isEqualTo("Avinguda Carlemany");
    }

    @Test
    public void should_add_alternative_road_names_with_unknow_language() {
        Map<String, String> tags = geocodeProvider.getAlternateRoadNamesWithSide(10200000001935L);
        assertThat(tags).hasSize(1);
        assertThat(tags.get("name")).isEqualTo("Carrer Maria Pla");
    }
}