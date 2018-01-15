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

    @Test
    public void should_return_a_way_alternate_names_file_from_name() {
        TomtomFolder tomtomFolder = new TomtomFolder("src/test/resources/tomtom/name/", "andorra");

        String smFiles = tomtomFolder.getFile("gc.dbf");

        assertThat(smFiles).isEqualTo("src/test/resources/tomtom/name/andorra___________gc.dbf");
    }

    @Test
    public void should_return_a_admin_level_alternate_names_file_from_name() {
        TomtomFolder tomtomFolder = new TomtomFolder("src/test/resources/tomtom/name/", "andorra");

        String smFiles = tomtomFolder.getFile("an.dbf");

        assertThat(smFiles).isEqualTo("src/test/resources/tomtom/name/andorra___________an.dbf");
    }

    @Test
    public void should_return_a_land_cover_alternate_names_file_from_name() {
        TomtomFolder tomtomFolder = new TomtomFolder("src/test/resources/tomtom/name/", "andorra");

        String smFiles = tomtomFolder.getFile("lxnm.dbf");

        assertThat(smFiles).isEqualTo("src/test/resources/tomtom/name/andorra___________lxnm.dbf");
    }
}