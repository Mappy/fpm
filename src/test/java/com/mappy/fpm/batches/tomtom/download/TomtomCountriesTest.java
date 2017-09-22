package com.mappy.fpm.batches.tomtom.download;

import org.junit.Test;

import static com.mappy.fpm.batches.tomtom.download.TomtomCountries.countries;
import static com.mappy.fpm.batches.tomtom.download.TomtomCountries.outerworld;
import static org.assertj.core.api.Assertions.assertThat;

public class TomtomCountriesTest {

    @Test
    public void should_have_all_european_countries() {
        assertThat(countries()).extracting("id").containsOnly("ALB", "AND", "AUT", "BEL", "BGR", "BIH",
                "BLR", "CHE", "CYP", "CZE", "DEU", "DNK", "ESP", "EST", "FIN", "FRA", "GBR", "GRC", "HRV", "HUN", "IRL",
                "ISL", "ITA", "LTU", "LUX", "LVA", "MDA", "MKD", "MLT", "MNE", "NLD", "NOR", "POL", "PRT", "ROU", "RUS",
                "SMR", "SRB", "SVK", "SVN", "SWE", "TUR", "UKR", "GLP", "GUF", "REU");
    }

    @Test
    public void should_have_all_outer_world_countries() {
        assertThat(outerworld()).extracting("id").containsOnly("OAT", "OIN", "OBE", "OCP", "ODE", "ODK",
                "OES", "OFI", "OFR", "OGB", "OGR", "OIE", "OIT", "ONL", "ONO", "OPL", "OPT", "ORU", "OSE", "OTR");
    }
}