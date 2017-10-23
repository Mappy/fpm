package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeDomainsParserTest {

    private final TimeDomainsParser parser = new TimeDomainsParser();

    @Test
    public void should_translate_tomtom_time_domain_to_osm_tag(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h11){h7}]");
        String osmTimeDomain = parser.parse(tomtomTimesDomains);

        assertThat(osmTimeDomain).isEqualTo("11:00-18:00 off");
    }

    @Test
    public void should_translate_tomtom_time_domain_over_midnight_to_osm_tag(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h22){h8}]");
        String osmTimeDomain = parser.parse(tomtomTimesDomains);

        assertThat(osmTimeDomain).isEqualTo("22:00-06:00 off");
    }
}