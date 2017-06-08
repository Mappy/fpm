package com.mappy.fpm.batches.geonames;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class GeonamesTest {

    private static Geonames geonames;

    @BeforeClass
    public static void init(){
        geonames = new Geonames(GeonamesTest.class.getResource("/geonames").getPath());
    }

    @Test
    public void should_find_french_name() throws Exception {
        assertThat(geonames.frenchNames(260114)).extracting(AlternateName::getValue).contains("La Canée");
        assertThat(geonames.frenchNames(2982652)).extracting(AlternateName::getValue).contains("Rouen");
        assertThat(geonames.frenchNames(11071624)).extracting(AlternateName::getValue).contains("Nord-Pas-de-Calais-Picardie", "Hauts-de-France");
    }

    @Test
    public void should_distinguish_alternate_names() throws Exception {
        assertThat(geonames.frenchNames(11071624)).containsExactly(
                new AlternateName("Hauts-de-France", false, false, false, false),
                new AlternateName("Nord-Pas-de-Calais-Picardie", false, false, false, true));
    }

    @Test
    public void should_find_alternate_name_by_country_code_iso3() throws Exception {
        assertThat(geonames.frenchNames("ESP")).containsExactly(
                new AlternateName("Espagne", true, false, false, false));

        assertThat(geonames.frenchNames("GBR")).containsExactly(
                new AlternateName("Royaume-Uni", true, false, false, false),
                new AlternateName("Grande-Bretagne", false, false, false, false));

        assertThat(geonames.frenchNames("UNKNOWN")).isEmpty();
    }
}
