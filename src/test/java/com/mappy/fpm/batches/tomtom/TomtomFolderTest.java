package com.mappy.fpm.batches.tomtom;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TomtomFolderTest {

    @Test
    public void should_return_all_sm_file_from_country() {
        TomtomFolder tomtomFolder = new TomtomFolder("src/test/resources/tomtom/town/", "country");

        List<String> smFiles = tomtomFolder.getSMFiles();

        assertThat(smFiles).containsOnly("src/test/resources/tomtom/town/country1_sm.shp", "src/test/resources/tomtom/town/country2_sm.shp");
    }
}