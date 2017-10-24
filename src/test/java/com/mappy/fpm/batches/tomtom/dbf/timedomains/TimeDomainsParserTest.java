package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class TimeDomainsParserTest {

    private final TimeDomainsParser parser = new TimeDomainsParser();

    @Test
    public void should_translate_tomtom_time_domain_to_osm_openinghours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h6){h2}]");
        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("06:00-08:00 off");
    }

    @Test
    public void should_translate_tomtom_time_domain_over_midnight_to_osm_openinghours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h22){h8}]");
        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("22:00-06:00 off");
    }

    @Test
    public void should_translate_tomtom_time_domain_collection_to_osm_openinghours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h6){h2}]");
        TimeDomains tomtomTimesDomains2 = new TimeDomains(14420000000590L, "[(h22){h8}]");

        String openingHours = parser.parse(newHashSet(tomtomTimesDomains, tomtomTimesDomains2));

        assertThat(openingHours).isEqualTo("06:00-08:00 off, 22:00-06:00 off");
    }
}