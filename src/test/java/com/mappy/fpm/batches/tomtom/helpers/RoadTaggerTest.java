package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.mappy.fpm.batches.tomtom.dbf.connectivity.ConnectivityTagger;
import com.mappy.fpm.batches.tomtom.dbf.geocodes.GeocodeProvider;
import com.mappy.fpm.batches.tomtom.dbf.intersection.RouteIntersectionProvider;
import com.mappy.fpm.batches.tomtom.dbf.lanes.LaneTagger;
import com.mappy.fpm.batches.tomtom.dbf.poi.FeatureType;
import com.mappy.fpm.batches.tomtom.dbf.poi.PoiProvider;
import com.mappy.fpm.batches.tomtom.dbf.routenumbers.RouteNumbersProvider;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPosts;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.SpeedProfiles;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestrictionTagger;
import com.mappy.fpm.batches.tomtom.dbf.restrictions.RestrictionTagger;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsParser;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsProvider;
import com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationAreaProvider;
import com.mappy.fpm.utils.MemoryFeature;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.utils.CollectionUtils.map;
import static com.mappy.fpm.utils.MemoryFeature.onlyTags;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoadTaggerTest {

    private final SpeedProfiles speedProfiles = mock(SpeedProfiles.class);
    private final SpeedRestrictionTagger speedRestrictionTagger = mock(SpeedRestrictionTagger.class);
    private final RestrictionTagger restrictionTagger = mock(RestrictionTagger.class);
    private final GeocodeProvider geocoding = mock(GeocodeProvider.class);
    private final TransportationAreaProvider transportationAreaProvider = mock(TransportationAreaProvider.class);
    private final SignPosts signPosts = mock(SignPosts.class);
    private final LaneTagger lanes = mock(LaneTagger.class);
    private final TimeDomainsProvider timeDomainsData = mock(TimeDomainsProvider.class);
    private final TimeDomainsParser timeDomainsParser = mock(TimeDomainsParser.class);
    private final TollTagger tollTagger = mock(TollTagger.class);
    private final ConnectivityTagger connectivityTagger = mock(ConnectivityTagger.class);
    private final RouteNumbersProvider routeNumbersProvider = mock(RouteNumbersProvider.class);
    private final RouteIntersectionProvider intersectionProvider = mock(RouteIntersectionProvider.class);
    private final PoiProvider poiProvider = mock(PoiProvider.class);
    private final Map<String, String> defaultTags = new HashMap<String, String>();
    private final RoadTagger tagger = new RoadTagger(speedProfiles, geocoding, signPosts, lanes, speedRestrictionTagger, restrictionTagger, tollTagger, connectivityTagger, timeDomainsData, timeDomainsParser, transportationAreaProvider, routeNumbersProvider, intersectionProvider, poiProvider);

    @Before
    public void setup() {
        when(speedProfiles.getTags(any(MemoryFeature.class))).thenReturn(newHashMap());
        when(geocoding.getLeftPostalCode(any(Long.class))).thenReturn(of("9120"));
        when(geocoding.getRightPostalCode(any(Long.class))).thenReturn(of("9130"));
        when(geocoding.getInterpolationsAddressLeft(any(Long.class))).thenReturn(of("even"));
        when(geocoding.getInterpolationsAddressRight(any(Long.class))).thenReturn(of("odd"));
        when(geocoding.getNamesAndAlternateNamesWithSide(any(Long.class))).thenReturn(ImmutableMap.of("name:left:fr", "name_left_fr" //
                , "alt_name:left:fr", "alt_name_left_fr", "name:right:fr", "name_right_fr"));
        when(transportationAreaProvider.getBuiltUpLeft(any(Long.class))).thenReturn(of("123"));
        when(transportationAreaProvider.getBuiltUpRight(any(Long.class))).thenReturn(of("456"));
        when(transportationAreaProvider.getSmallestAreasLeft(any(Long.class))).thenReturn(of("789"));
        when(transportationAreaProvider.geSmallestAreasRight(any(Long.class))).thenReturn(of("112"));
        when(routeNumbersProvider.getInternationalRouteNumbers(any(Long.class))).thenReturn(of("E41"));
        when(routeNumbersProvider.getNationalRouteNumbers(any(Long.class))).thenReturn(of("N5"));
        when(routeNumbersProvider.getRouteTypeOrderByPriority(any(Long.class))).thenReturn(of("5"));
        when(poiProvider.getPoiNameByType(any(Long.class), eq(FeatureType.MOUNTAIN_PASS.getValue()))).thenReturn(empty());
	setDefaultTags();
    }

    private void setDefaultTags() {
	defaultTags.clear();
	defaultTags.put("FT", "0");
	defaultTags.put("ID", "123");
	defaultTags.put("F_ELEV", "0");
	defaultTags.put("T_ELEV", "0");
	defaultTags.put("RDCOND", "1");
    }


    @Test
    public void should_add_intersection_exit() {
        when(intersectionProvider.getIntersectionById())
                .thenReturn(ImmutableMap.of(123L, "exit 13"));
	defaultTags.put("FOW", "10");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("junction:ref", "exit 13");
    }

    @Test
    public void should_add_level_tag() {
	defaultTags.put("F_ELEV",  "0");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("layer", "0");

	defaultTags.put("F_ELEV",  "1");
	defaultTags.put("T_ELEV",  "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("layer", "1");

	defaultTags.put("F_ELEV",  "0");
	defaultTags.put("T_ELEV",  "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("layer:from", "0").containsEntry("layer:to", "1");
    }

    @Test
    public void should_tag_roundabout() {
	defaultTags.put("FOW",  "4");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("junction", "roundabout");

	defaultTags.put("FOW",  "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .doesNotContainEntry("junction", "roundabout");
    }

    @Test
    public void should_tag_ferries() {
	defaultTags.put("FT", "1");
	defaultTags.put("NAME", "Calais - Douvres");
	defaultTags.put("MINUTES", "10.902");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("route", "ferry")
                .containsEntry("name", "Calais - Douvres")
                .containsEntry("duration", "00:10:54")
                .containsEntry("name:left:fr", "name_left_fr");

	defaultTags.put("FT", "1");
	defaultTags.put("MINUTES", "10");
	defaultTags.remove("NAME");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("route", "ferry").doesNotContainKey("name");
    }

    @Test
    public void should_tag_ferries_without_cars() {
	defaultTags.put("FT", "1");
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("MINUTES", "10.902");
	defaultTags.put("FOW", "14");
	defaultTags.put("ONEWAY", "N");
	defaultTags.put("NAME", "Calais - Douvres");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("route", "ferry").containsEntry("name", "Calais - Douvres").containsEntry("duration", "00:10:54");
    }

    @Test
    public void should_tag_pedestrian_roads() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("MINUTES", "10");

	defaultTags.put("FOW", "14");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "pedestrian");

	defaultTags.put("FOW", "15");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "footway");

	defaultTags.put("FOW", "19");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "steps");

	defaultTags.put("FOW", "3");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .doesNotContainEntry("highway", "steps");
    }

    @Test
    public void should_tag_privateRoads() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("MINUTES", "10");
	defaultTags.put("FOW", "3");

	defaultTags.put("PRIVATERD", "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("access", "private");

	defaultTags.put("PRIVATERD", "2");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("access", "private");

	defaultTags.put("PRIVATERD", "0");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
	    .doesNotContainKey("access");
    }

    @Test
    public void should_tag_foot_and_bicycle_no() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("MINUTES", "10");

	defaultTags.put("RAMP", "1");

	defaultTags.put("FOW", "3");
	defaultTags.put("FRC", "0");
	defaultTags.put("FREEWAY", "0");

        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("foot", "no").containsEntry("bicycle", "no");

	defaultTags.put("FOW", "1");
	defaultTags.put("FRC", "1");
	defaultTags.put("FREEWAY", "0");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("foot", "no").containsEntry("bicycle", "no");

	defaultTags.put("FOW", "3");
	defaultTags.put("FRC", "0");
	defaultTags.put("FREEWAY", "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("foot", "no").containsEntry("bicycle", "no");
    }

    @Test
    public void should_tag_road_type() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("MINUTES", "10");
	defaultTags.put("FOW", "3");
	defaultTags.put("RAMP", "0");


	defaultTags.put("FRC", "0");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "motorway");

	defaultTags.put("FRC", "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "trunk");

	defaultTags.put("FRC", "2");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "primary");

	defaultTags.put("FRC", "3");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "secondary");

	defaultTags.put("FRC", "5");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "tertiary");

	defaultTags.put("FRC", "7");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "residential");
    }

    @Test
    public void should_add_mappy_length_tag() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "14");
	defaultTags.put("METERS", "150.25");

        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("mappy_length", "150.25");
    }

    @Test
    public void should_add_bridge_and_tunnel() {
	defaultTags.put("PARTSTRUC", "0");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .doesNotContainKey("tunnel").doesNotContainKey("bridge");

	defaultTags.put("PARTSTRUC", "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("tunnel", "yes");

	defaultTags.put("PARTSTRUC", "2");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("bridge", "yes");
    }

    @Test
    public void should_tag_motorway_link() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("MINUTES", "10");
	defaultTags.put("FOW", "10");

	defaultTags.put("FRC", "0");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "motorway_link");

	defaultTags.put("FRC", "1");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "trunk_link");


	defaultTags.put("FRC", "2");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "primary_link");

	defaultTags.put("FRC", "3");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "secondary_link");

	defaultTags.put("FRC", "7");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "tertiary_link");

    }

    @Test
    public void should_add_ref_tag() {
        when(routeNumbersProvider.getInternationalRouteNumbers(any(Long.class))).thenReturn(empty());
        when(routeNumbersProvider.getNationalRouteNumbers(any(Long.class))).thenReturn(empty());

	defaultTags.put("FT", "0");
	defaultTags.put("SHIELDNUM", "A13");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("int_ref", "A13");

	defaultTags.remove("SHIELDNUM");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .doesNotContainKey("ref");
    }

    @Test
    public void should_tag_a_highway_service() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("FRC", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "service");
    }

    @Test
    public void should_tag_is_in() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("FRC", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("is_in:left", "9120")
                .containsEntry("is_in:right", "9130");
    }

    @Test
    public void should_tag_interpolation() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("FRC", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("addr:interpolation:left", "even")
                .containsEntry("addr:interpolation:right", "odd");
    }

    @Test
    public void should_tag_left_and_interpolation_address() {
        when(geocoding.getInterpolations(any(Long.class))).thenReturn(ImmutableMap.of("interpolation:left", "1;10", "interpolation:right", "11;20"));


	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("FRC", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("interpolation:left", "1;10")
                .containsEntry("interpolation:right", "11;20");
    }

    @Test
    public void should_have_a_global_importance() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("NET2CLASS", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("global_importance:tomtom", "6");
    }

    @Test
    public void should_have_built_up_ids() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("NET2CLASS", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("bua:tomtom:left", "123")
                .containsEntry("bua:tomtom:right", "456");
    }

    @Test
    public void should_have_areas_ids() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("NET2CLASS", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("admin:tomtom:left", "789")
                .containsEntry("admin:tomtom:right", "112");
    }

    @Test
    public void should_have_route_numbers_and_type() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("NET2CLASS", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("int_ref", "E41")
                .containsEntry("ref", "N5")
                .containsEntry("route_type:tomtom", "5");
    }

    @Test
    public void should_have_steps() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "19");
	defaultTags.put("NET2CLASS", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "steps");
    }

    @Test
    public void should_have_road_authorities() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "20");
	defaultTags.put("NET2CLASS", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("highway", "service")
                .containsEntry("service", "emergency_access");
    }

    @Test
    public void should_have_mountain_pass() {
        when(poiProvider.getPoiNameByType(any(Long.class), eq(FeatureType.MOUNTAIN_PASS.getValue()))).thenReturn(of("everest"));

	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("NAME", "not everest");
	defaultTags.put("FOW", "20");
	defaultTags.put("NET2CLASS", "6");

        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("mountain_pass", "everest");
    }

    @Test
    public void should_have_a_functional_road_class() {
	defaultTags.put("FRC", "7");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("frc:tomtom", "7");
    }

    @Test
    public void should_have_a_form_of_way() {
	defaultTags.put("FOW", "11");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("fow:tomtom", "11");
    }

    @Test
    public void should_add_Alternate_RoadNames_With_Side() {
	defaultTags.put("FEATTYP", "4110");
	defaultTags.put("FOW", "11");
	defaultTags.put("FRC", "6");
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("name:left:fr", "name_left_fr")
                .containsEntry("name:right:fr", "name_right_fr")
                .containsEntry("alt_name:left:fr", "alt_name_left_fr");

    }

    @Test
    public void should_have_paved_info() {
        assertThat(tagger.tagData(onlyTags(defaultTags)))
                .containsEntry("surface", "paved");

	for(String value: new String[]{"0", "2", "3"}) {
	    defaultTags.put("RDCOND", value);
	    assertThat(tagger.tagData(onlyTags(defaultTags))) .containsEntry("surface", "unpaved");
	}
    }

    @Test
    public void should_have_source() {
        assertThat(tagger.tagMetadata("mockcountry", "mockzone"))
                .containsEntry("source:country:download_job", "mockcountry")
                .containsEntry("source:zone:tomtom", "mockzone");
    }

    @Test
    public void should_add_is_exit() {
    	defaultTags.put("RAMP", "1");
      assertThat(tagger.tagData(onlyTags(defaultTags)))
              .containsEntry("ramp_type", "exit");

    	defaultTags.put("RAMP", "2");
            assertThat(tagger.tagData(onlyTags(defaultTags)))
            .containsEntry("ramp_type", "entrance");
    }

    @Test
    public void should_tag_construction() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("highway", "primary");
        tags.put("construction", "both");
        tags.put("construction_start_expected", "2020-02-02");
        tags.put("construction_end_expected", "2021-02-02");

        tagger.tagConstruction(tags);
        assertThat(tags)
                .containsEntry("construction", "primary")
                .containsEntry("highway", "construction")
                .containsEntry("construction_start_expected", "2020-02-02")
                .containsEntry("construction_end_expected", "2021-02-02");
    }

    @Test
    public void should_tag_construction_forward() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("highway", "motorway");
        tags.put("construction", "forward");
        tags.put("construction_start_expected", "2020-02-02");
        tags.put("construction_end_expected", "2021-02-02");

        tagger.tagConstruction(tags);

        assertThat(tags)
                .containsEntry("construction:forward", "motorway")
                .containsEntry("highway", "construction")
                .containsEntry("construction_start_expected", "2020-02-02")
                .containsEntry("construction_end_expected", "2021-02-02");
    }

    @Test
    public void should_tag_construction_backward() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("highway", "secondary");
        tags.put("construction", "backward");
        tags.put("construction_start_expected", "2020-02-02");
        tags.put("construction_end_expected", "2021-02-02");

        tagger.tagConstruction(tags);

        assertThat(tags)
                .containsEntry("construction:backward", "secondary")
                .containsEntry("highway", "construction")
                .containsEntry("construction_start_expected", "2020-02-02")
                .containsEntry("construction_end_expected", "2021-02-02");
    }

    @Test
    public void should_tag_construction_oneway() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("oneway", "yes");
        tags.put("highway", "primary");
        tags.put("construction", "both");
        tags.put("construction_start_expected", "2020-02-02");
        tags.put("construction_end_expected", "2021-02-02");

        tagger.tagConstruction(tags);
        assertThat(tags)
                .containsEntry("construction", "primary")
                .containsEntry("highway", "construction")
                .containsEntry("construction_start_expected", "2020-02-02")
                .containsEntry("construction_end_expected", "2021-02-02");
    }

    @Test
    public void should_tag_construction_oneway_forward() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("oneway", "yes");
        tags.put("highway", "primary");
        tags.put("construction", "forward");
        tags.put("construction_start_expected", "2020-02-02");
        tags.put("construction_end_expected", "2021-02-02");

        tagger.tagConstruction(tags);
        assertThat(tags)
                .containsEntry("construction", "primary")
                .containsEntry("highway", "construction")
                .containsEntry("construction_start_expected", "2020-02-02")
                .containsEntry("construction_end_expected", "2021-02-02");
    }

}
