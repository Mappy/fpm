package com.mappy.fpm.batches.tomtom.dbf.names;

import com.mappy.fpm.batches.tomtom.TomtomFolder;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NameProviderTest {

    @Test
    public void shouldAddAlternativeName(){
        NameProvider np = new NameProvider(new TomtomFolder("src/test/resources/osmgenerator/", "belbe2"));
        np.loadFromFile("smnm.dbf", "NAME", false);
        Map<String, String> tags = new HashMap<>();
        tags.putAll(np.getAlternateNames(10560000430948L));
        assertEquals(tags.size(), 5);
        assertEquals(tags.get("name:fr"), "Bruxelles");
        assertEquals(tags.get("name:nl"), "Brussel");
        assertEquals(tags.get("name:de"), "Brüssel");
        assertEquals(tags.get("name:en"), "Brussels");
        assertEquals(tags.get("name:es"), "Bruselas");
    }

    @Test
    public void shouldAddAlternativeFullName(){
        NameProvider np = new NameProvider(new TomtomFolder("src/test/resources/osmgenerator/", "belbe2"));
        np.loadFromFile("gc.dbf", "FULLNAME", false);
        Map<String, String> tags = new HashMap<>();
        tags.putAll(np.getAlternateNames(-2147483648L));
        assertEquals(3, tags.size());
        assertEquals("Waverse Steenweg", tags.get("name:nl"));
        assertEquals("A34", tags.get("alt_name"));

    }
}