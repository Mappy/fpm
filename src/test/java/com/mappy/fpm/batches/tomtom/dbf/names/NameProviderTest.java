package com.mappy.fpm.batches.tomtom.dbf.names;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NameProviderTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);
    private NameProvider nameProvider;

    @Before
    public void setUp() {
        when(tomtomFolder.getFile("an.dbf")).thenReturn("src/test/resources/tomtom/name/andorra___________an.dbf");
        when(tomtomFolder.getFile("lxnm.dbf")).thenReturn("src/test/resources/tomtom/name/andorra___________lxnm.dbf");
        when(tomtomFolder.getFile("smnm.dbf")).thenReturn("src/test/resources/tomtom/name/andorra___________smnm.dbf");
        nameProvider = new NameProvider(tomtomFolder);
    }

    @Test
    public void should_add_alternative_names() {
        nameProvider.loadAlternateNames("an.dbf");
        Map<String, String> tags = nameProvider.getAlternateNames(10200000000008L);
        assertThat(tags).hasSize(7);
        assertThat(tags.get("name:ca")).isEqualTo("Andorra_cat");
        assertThat(tags.get("name:fr")).isEqualTo("Andorre");
        assertThat(tags.get("alt_name:fr")).isEqualTo("Principauté d'andorre");
        assertThat(tags.get("name:de")).isEqualTo("Andorra_ger");
        assertThat(tags.get("name:en")).isEqualTo("Andorra_eng");
        assertThat(tags.get("name:es")).isEqualTo("Andorra_spa");
        assertThat(tags.get("int_name")).isEqualTo("Andorra_aaa");
    }

    @Test
    public void should_add_alternative_city_names() {
        nameProvider.loadAlternateCityNames();
        Map<String, String> tags = nameProvider.getAlternateCityNames(10200000000008L);
        assertThat(tags).hasSize(7);
        assertThat(tags.get("name:ca")).isEqualTo("Andorra_cat");
        assertThat(tags.get("name:fr")).isEqualTo("Andorre");
        assertThat(tags.get("alt_name:fr")).isEqualTo("Principauté d'andorre");
        assertThat(tags.get("name:de")).isEqualTo("Andorra_ger");
        assertThat(tags.get("name:en")).isEqualTo("Andorra_eng");
        assertThat(tags.get("name:es")).isEqualTo("Andorra_spa");
        assertThat(tags.get("int_name")).isEqualTo("Andorra_aaa");
    }

    @Test
    public void should_add_alternative_names_for_land_cover() {
        nameProvider.loadAlternateNames("lxnm.dbf");
        Map<String, String> tags = nameProvider.getAlternateNames(10200000000008L);
        assertThat(tags).hasSize(7);
        assertThat(tags.get("name:ca")).isEqualTo("Andorra_aeropurta");
        assertThat(tags.get("name:fr")).isEqualTo("Aeroport d'Andorre");
        assertThat(tags.get("alt_name:fr")).isEqualTo("Aeroport de la Principauté d'andorre");
        assertThat(tags.get("name:de")).isEqualTo("Andorra_airpurten");
        assertThat(tags.get("name:en")).isEqualTo("Andorra_airport");
        assertThat(tags.get("name:es")).isEqualTo("Andorra_aeropurto");
        assertThat(tags.get("int_name")).isEqualTo("Andorra_aaa");
    }

}