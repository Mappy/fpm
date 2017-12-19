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
}