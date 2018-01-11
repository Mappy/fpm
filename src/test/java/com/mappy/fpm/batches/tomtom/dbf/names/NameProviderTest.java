package com.mappy.fpm.batches.tomtom.dbf.names;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.jamel.dbf.structure.DbfRow;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NameProviderTest {

    private final TomtomFolder tomtomFolder = mock(TomtomFolder.class);
    private NameProvider nameProvider;

    @Before
    public void setUp() {
        when(tomtomFolder.getFile("an.dbf")).thenReturn("src/test/resources/tomtom/name/andorra___________an.dbf");
        when(tomtomFolder.getFile("gc.dbf")).thenReturn("src/test/resources/tomtom/name/andorra___________gc.dbf");
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
        assertThat(tags.get("alt_name")).isEqualTo("Andorra_aaa");
    }

    @Test
    public void should_add_alternative_road_names() {
        nameProvider.loadAlternateNames("gc.dbf");
        Map<String, String> tags = nameProvider.getAlternateRoadSideNames(10200000000008L, 1);
        assertThat(tags).hasSize(4);
        assertThat(tags.get("name:left")).isEqualTo("Andorra_eng");
        assertThat(tags.get("name:left:en")).isEqualTo("Andorra_eng");
        assertThat(tags.get("name:right")).isEqualTo("Andorre");
        assertThat(tags.get("name:right:fr")).isEqualTo("Andorre");
    }
}