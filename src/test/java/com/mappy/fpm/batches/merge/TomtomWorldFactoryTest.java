package com.mappy.fpm.batches.merge;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TomtomWorldFactoryTest {

    private TomtomWorldFactory factory;

    @Test
    public void should_load_tomtom_countries() throws Exception {
        factory = new TomtomWorldFactory("src/test/resources/merge/tomtomfiles");

        TomtomWorld tomtomWorld = factory.loadTomtom();

        assertThat(tomtomWorld.getCountries()).extracting("name").containsOnly("Andorra", "Guyane Française");
    }
}