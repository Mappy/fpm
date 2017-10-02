package com.mappy.fpm.batches.tomtom.helpers;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OsmLevelGeneratorTest {

    private final OsmLevelGenerator osmLevelGenerator = new OsmLevelGenerator();

    @Test
    public void should_return_osm_level_2_for_all_tomtom_level_0() {
        String osmLevel = osmLevelGenerator.getOsmLevel("belbe2", "0");

        assertThat(osmLevel).isEqualTo("2");
    }

    @Test
    public void should_return_osm_level_4_for_all_tomtom_level_1() {
        String osmLevel = osmLevelGenerator.getOsmLevel("deud51", "1");

        assertThat(osmLevel).isEqualTo("4");
    }

    @Test
    public void should_return_osm_level_6_for_all_tomtom_level_2() {
        String osmLevel = osmLevelGenerator.getOsmLevel(null, "2");

        assertThat(osmLevel).isEqualTo("6");
    }

    @Test
    public void should_return_osm_level_6_for_all_tomtom_level_7() {
        String osmLevel = osmLevelGenerator.getOsmLevel("fraf22", "7");

        assertThat(osmLevel).isEqualTo("6");
    }

    @Test
    public void should_return_osm_level_7_for_belgium_tomtom_level_7() {
        assertThat(osmLevelGenerator.getOsmLevel("belbe2", "7")).isEqualTo("7");
        assertThat(osmLevelGenerator.getOsmLevel("belbe3", "7")).isEqualTo("7");
    }

    @Test
    public void should_return_osm_level_8_for_all_tomtom_level_8() {
        String osmLevel = osmLevelGenerator.getOsmLevel(null, "8");

        assertThat(osmLevel).isEqualTo("8");
    }

    @Test
    public void should_return_osm_level_9_for_all_tomtom_level_9() {
        String osmLevel = osmLevelGenerator.getOsmLevel(null, "9");

        assertThat(osmLevel).isEqualTo("9");
    }

    @Test
    public void should_return_osm_level_10_for_germany_tomtom_level_9() {
        String osmLevel = osmLevelGenerator.getOsmLevel("deud51", "9");

        assertThat(osmLevel).isEqualTo("10");
    }

}