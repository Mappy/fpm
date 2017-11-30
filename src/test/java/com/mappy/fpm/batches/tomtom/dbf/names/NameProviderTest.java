package com.mappy.fpm.batches.tomtom.dbf.names;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NameProviderTest {

    @Test
    public void should_add_alternative_names() {
        NameProvider np = new NameProvider(new TomtomFolder("src/test/resources/tomtom/", "andorra"));
        np.loadFromFile("an.dbf");
        Map<String, String> tags = np.getAlternateNames(10200000000008L);
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