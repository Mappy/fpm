package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.tomtom.dbf.geocodes.GeocodeProvider;
import com.mappy.fpm.batches.tomtom.dbf.lanes.LaneTagger;
import com.mappy.fpm.batches.tomtom.dbf.routenumbers.RouteNumbersProvider;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPosts;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.SpeedProfiles;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestrictionTagger;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsParser;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsProvider;
import com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationAreaProvider;
import com.mappy.fpm.utils.MemoryFeature;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.utils.CollectionUtils.map;
import static com.mappy.fpm.utils.MemoryFeature.onlyTags;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoadTaggerTest {

    private final SpeedProfiles speedProfiles = mock(SpeedProfiles.class);
    private final SpeedRestrictionTagger speedRestrictionTagger = mock(SpeedRestrictionTagger.class);
    private final GeocodeProvider geocoding = mock(GeocodeProvider.class);
    private final TransportationAreaProvider transportationAreaProvider = mock(TransportationAreaProvider.class);
    private final SignPosts signPosts = mock(SignPosts.class);
    private final LaneTagger lanes = mock(LaneTagger.class);
    private final TimeDomainsProvider timeDomainsData = mock(TimeDomainsProvider.class);
    private final TimeDomainsParser timeDomainsParser = mock(TimeDomainsParser.class);
    private final TollTagger tollTagger = mock(TollTagger.class);
    private final RouteNumbersProvider routeNumbersProvider = mock(RouteNumbersProvider.class);
    private final RoadTagger tagger = new RoadTagger(speedProfiles, geocoding, signPosts, lanes, speedRestrictionTagger, tollTagger, timeDomainsData, timeDomainsParser, transportationAreaProvider, routeNumbersProvider);

    @Before
    public void setup() {
        when(speedProfiles.getTags(any(MemoryFeature.class))).thenReturn(newHashMap());
        when(geocoding.getLeftAndRightPostalCode(any(Long.class))).thenReturn(of("9120"));
        when(geocoding.getInterpolations(any(Long.class))).thenReturn(of("even;odd"));
        when(transportationAreaProvider.getBuiltUp(any(Long.class))).thenReturn(of("123;456"));
        when(transportationAreaProvider.getSmallestAreas(any(Long.class))).thenReturn(of("789;112"));
        when(routeNumbersProvider.getInternationalRouteNumbers(any(Long.class))).thenReturn(of("E41"));
        when(routeNumbersProvider.getNationalRouteNumbers(any(Long.class))).thenReturn(of("N5"));
    }

    @Test
    public void should_add_level_tag() {
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0"))))
                .containsEntry("layer", "0");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "1", "T_ELEV", "1"))))
                .containsEntry("layer", "1");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "1"))))
                .containsEntry("layer:from", "0").containsEntry("layer:to", "1");
    }

    @Test
    public void should_tag_roundabout() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "4"))))
                .containsEntry("junction", "roundabout");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "1"))))
                .doesNotContainEntry("junction", "roundabout");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "4"))))
                .containsEntry("junction", "roundabout");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "1"))))
                .doesNotContainEntry("junction", "roundabout");
    }

    @Test
    public void should_tag_ferries() {
        assertThat(tagger.tag(onlyTags(map("ID", "123", "FT", "1", "F_ELEV", "0", "T_ELEV", "0", "NAME", "Calais - Douvres", "MINUTES", "10.902"))))
                .containsEntry("route", "ferry").containsEntry("name", "Calais - Douvres").containsEntry("duration", "00:10:54");
        assertThat(tagger.tag(onlyTags(map("ID", "123", "FT", "1", "F_ELEV", "0", "T_ELEV", "0", "MINUTES", "10", "", ""))))
                .containsEntry("route", "ferry").doesNotContainKey("name");
    }

    @Test
    public void should_tag_ferries_without_cars() {
        assertThat(tagger.tag(onlyTags(map("FT", "1", "FEATTYP", "4110", "ID", "123", "MINUTES", "10.902", "F_ELEV", "0", "T_ELEV", "0", "FOW", "14", "NAME", "Calais - Douvres", "ONEWAY", "N"))))
                .containsEntry("route", "ferry").containsEntry("name", "Calais - Douvres").containsEntry("duration", "00:10:54").containsEntry("motor_vehicle", "no");
    }

    @Test
    public void should_tag_motor_vehicle_no() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "ONEWAY", "N")))) //
                .containsEntry("motor_vehicle", "no");
    }

    @Test
    public void should_not_tag_motor_vehicle_no_when_restriction_speed() {
        List<TimeDomains> timeDomainList = newArrayList(new TimeDomains(1L, null));
        when(timeDomainsData.getTimeDomains(any(Long.class))).thenReturn(timeDomainList);

        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "ONEWAY", "N")))) //
                .doesNotContainEntry("motor_vehicle", "no");
    }

    @Test
    public void should_tag_pedestrian_roads() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "14"))))
                .containsEntry("highway", "pedestrian");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "15"))))
                .containsEntry("highway", "footway");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "19"))))
                .containsEntry("highway", "steps");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3"))))
                .doesNotContainEntry("highway", "steps");
    }

    @Test
    public void should_tag_privateRoads() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "PRIVATERD", "1"))))
                .containsEntry("access", "private");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "PRIVATERD", "2"))))
                .containsEntry("access", "private");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "PRIVATERD", "0")))).doesNotContainKey("access");
    }

    @Test
    public void should_tag_foot_and_bicycle_no() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "FRC", "0", "FREEWAY", "0", "RAMP", "1"))))
                .containsEntry("foot", "no").containsEntry("bicycle", "no");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FOW", "1", "FRC", "1", "FREEWAY", "0", "RAMP", "1"))))
                .containsEntry("foot", "no").containsEntry("bicycle", "no");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "FRC", "0", "FREEWAY", "1", "RAMP", "1"))))
                .containsEntry("foot", "no").containsEntry("bicycle", "no");
    }

    @Test
    public void should_tag_road_type() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "FRC", "0", "RAMP", "0"))))
                .containsEntry("highway", "motorway");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "FRC", "5", "RAMP", "0"))))
                .containsEntry("highway", "secondary");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "FRC", "7", "RAMP", "0"))))
                .containsEntry("highway", "residential");
    }

    @Test
    public void should_add_mappy_length_tag() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "feattyp", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "fow", "14", "METERS", "150.25"))))
                .containsEntry("mappy_length", "150.25");
    }

    @Test
    public void should_add_bridge_and_tunnel() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "PARTSTRUC", "0"))))
                .doesNotContainKey("tunnel").doesNotContainKey("bridge");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "PARTSTRUC", "1"))))
                .containsEntry("tunnel", "yes");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "PARTSTRUC", "2"))))
                .containsEntry("bridge", "yes");
    }

    @Test
    public void should_tag_motorway_link() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "10", "FRC", "0"))))
                .containsEntry("highway", "motorway_link");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "10", "FRC", "7"))))
                .containsEntry("highway", "tertiary_link");
    }

    @Test
    public void should_tag_oneway() {
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).containsEntry("oneway", "yes");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "TF", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).containsEntry("oneway", "yes");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "N", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).doesNotContainKey("oneway");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).doesNotContainKey("oneway");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).doesNotContainKey("oneway");
    }

    @Test
    public void should_add_ref_tag() {
        when(routeNumbersProvider.getInternationalRouteNumbers(any(Long.class))).thenReturn(Optional.empty());
        when(routeNumbersProvider.getNationalRouteNumbers(any(Long.class))).thenReturn(Optional.empty());

        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "SHIELDNUM", "A13"))))
                .containsEntry("int_ref", "A13");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0"))))
                .doesNotContainKey("ref");
    }

    @Test
    public void should_add_opening_hours_tag() {
        TimeDomains domainTomtom = new TimeDomains(456, "domainetomtom");
        TimeDomains domainTomtom2 = new TimeDomains(789, "domainetomtom2");
        List<TimeDomains> timeDomains = newArrayList(domainTomtom, domainTomtom2);
        when(timeDomainsData.getTimeDomains(123)).thenReturn(timeDomains);
        when(timeDomainsParser.parse(timeDomains)).thenReturn("10:00-14:00 off, 22:00-06:00 off");
        MemoryFeature feature = onlyTags(ImmutableMap.of("ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FT", "0"));

        assertThat(tagger.tag(feature)).containsEntry("opening_hours", "10:00-14:00 off, 22:00-06:00 off");
    }

    @Test
    public void should_ignore_non_meaning_time_domain() {
        TimeDomains domainTomtom = new TimeDomains(456, "domainetomtom");
        List<TimeDomains> timeDomains = newArrayList(domainTomtom);
        when(timeDomainsData.getTimeDomains(123)).thenReturn(timeDomains);
        when(timeDomainsParser.parse(timeDomains)).thenReturn("");
        MemoryFeature feature = onlyTags(ImmutableMap.of("ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FT", "0"));

        assertThat(tagger.tag(feature)).doesNotContainKeys("opening_hours");
    }

    @Test
    public void should_ignore_non_parsable_time_domain() {
        TimeDomains domainTomtom = new TimeDomains(456, "domainetomtom");
        List<TimeDomains> timeDomains = newArrayList(domainTomtom);
        when(timeDomainsData.getTimeDomains(123)).thenReturn(timeDomains);
        when(timeDomainsParser.parse(timeDomains)).thenThrow(new IllegalArgumentException("Test exception"));
        MemoryFeature feature = onlyTags(ImmutableMap.of("ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FT", "0"));

        assertThat(tagger.tag(feature)).doesNotContainKeys("opening_hours");
    }

    @Test
    public void should_tag_a_highway_service() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "11", "FRC", "6"))))
                .containsEntry("highway", "service");
    }

    @Test
    public void should_tag_is_in() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "11", "FRC", "6"))))
                .containsEntry("is_in", "9120");
    }

    @Test
    public void should_tag_interpolation() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "11", "FRC", "6"))))
                .containsEntry("addr:interpolation", "even;odd");
    }

    @Test
    public void should_have_a_global_importance() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "11", "NET2CLASS", "6"))))
                .containsEntry("global_importance:tomtom", "6");
    }

    @Test
    public void should_have_built_up_ids() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "11", "NET2CLASS", "6"))))
                .containsEntry("bua:tomtom", "123;456");
    }

    @Test
    public void should_have_areas_ids() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "11", "NET2CLASS", "6"))))
                .containsEntry("admin:tomtom", "789;112");
    }

    @Test
    public void should_have_route_numbers() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "11", "NET2CLASS", "6"))))
                .containsEntry("int_ref", "E41")
                .containsEntry("ref", "N5");
    }

    @Test
    public void should_have_steps() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "19", "NET2CLASS", "6"))))
                .containsEntry("highway", "steps");
    }

    @Test
    public void should_have_road_authorities() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "20", "NET2CLASS", "6"))))
                .containsEntry("highway", "service")
                .containsEntry("service", "emergency_access");
    }
}
