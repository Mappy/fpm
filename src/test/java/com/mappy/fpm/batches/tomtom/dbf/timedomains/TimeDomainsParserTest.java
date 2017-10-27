package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class TimeDomainsParserTest {

    private final TimeDomainsParser parser = new TimeDomainsParser();

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_time_domain_could_not_be_parsed() {
        parser.parse(newHashSet(new TimeDomains(144, "non-parsable")));
    }

    @Test
    public void should_translate_tomtom_time_domain_collection_to_osm_opening_hours_string(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h6){h2}]");
        TimeDomains tomtomTimesDomains2 = new TimeDomains(14420000000590L, "[(h12)(h22)]");

        String openingHours = parser.parse(newHashSet(tomtomTimesDomains, tomtomTimesDomains2));

        assertThat(openingHours).isEqualTo("06:00-08:00 off, 12:00-22:00 off");
    }

    // INTERVAL

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_string_when_time_interval_could_not_be_parsed() {
        parser.parse(newHashSet(new TimeDomains(144, "[(Z11)(Q23)]")));
    }

    @Test
    public void should_translate_tomtom_month_interval_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(M3)(M5)]");

        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("Mar-May off");
    }


    // DURATION

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_string_when_time_duration_could_not_be_parsed() {
        parser.parse(newHashSet(new TimeDomains(144, "[(Z11){Q23}]")));
    }

    @Test
    public void should_return_empty_string_when_time_duration_has_z_mode() {

        String openingHours = parser.parse(newHashSet(new TimeDomains(144, "[(z37){z87}]")));

        assertThat(openingHours).isEqualTo("");
    }

    @Test
    public void should_translate_tomtom_hour_duration_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h6){h2}]");
        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("06:00-08:00 off");
    }

    @Test
    public void should_translate_tomtom_hour_duration_over_midnight_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h22){h8}]");
        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("22:00-06:00 off");
    }

    @Test
    public void should_translate_tomtom_hour_and_minutes_duration_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(h14m15){h1m15}])]");
        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("14:15-15:30 off");
    }

    @Test
    public void should_translate_tomtom_month_duration_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(M3){M5}]");

        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("Mar-Jul off");
    }

    @Test
    public void should_translate_tomtom_month_duration_over_new_year_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(M10){M5}]");

        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("Oct-Feb off");
    }

    @Test
    public void should_translate_tomtom_weekday_with_hour_duration_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(t1){h1}]");

        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("Su 00:00-01:00 off");
    }

    @Test
    public void should_translate_tomtom_weekday_and_hour_with_hour_duration_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(t2h5){h1}]");

        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("Mo 05:00-06:00 off");
    }

    @Test
    public void should_translate_multiple_tomtom_weekday_and_hour_with_hour_duration_to_osm_opening_hours(){
        TimeDomains tomtomTimesDomains = new TimeDomains(14420000000590L, "[(t2t6){h10}]");

        String osmTimeDomain = parser.parse(newHashSet(tomtomTimesDomains));

        assertThat(osmTimeDomain).isEqualTo("Mo,Fr 00:00-10:00 off");
    }

}