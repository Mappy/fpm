package com.mappy.fpm.batches.tomtom.dbf.names;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NameProviderTest {

    @Test
    public void should_add_alternative_names() {
        NameProvider np = new NameProvider(new TomtomFolder("src/test/resources/tomtom/", "andorra"));
        np.loadFromFile("an.dbf", "NAME", false);
        Map<String, String> tags = np.getAlternateNames(10200000000008L);
        assertEquals(tags.size(), 6);
        assertEquals(tags.get("name:ca"), "Andorra_cat");
        assertEquals(tags.get("name:fr"), "Andorre");
        assertEquals(tags.get("name:de"), "Andorra_ger");
        assertEquals(tags.get("name:en"), "Andorra_eng");
        assertEquals(tags.get("name:es"), "Andorra_spa");
        assertEquals(tags.get("alt_name"), "Andorra_aaa");
    }
}