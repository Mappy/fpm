package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mappy.fpm.batches.tomtom.TomtomStats;
import com.mappy.fpm.batches.tomtom.dbf.lanes.LaneTagger;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.dbf.signposts.SignPosts;
import com.mappy.fpm.batches.tomtom.dbf.speedprofiles.SpeedProfiles;
import com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestrictionTagger;
import com.mappy.fpm.utils.MemoryFeature;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.mappy.fpm.batches.utils.CollectionUtils.map;
import static com.mappy.fpm.utils.MemoryFeature.onlyTags;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoadTaggerTest {
    private final SpeedProfiles speedProfiles = mock(SpeedProfiles.class);
    private final SpeedRestrictionTagger speedRestrictionTagger = mock(SpeedRestrictionTagger.class);
    private final NameProvider names = mock(NameProvider.class);
    private final SignPosts signPosts = mock(SignPosts.class);
    private final LaneTagger lanes = mock(LaneTagger.class);
    private final RoadTagger tagger = new RoadTagger(speedProfiles, mock(TomtomStats.class), names, signPosts, lanes, speedRestrictionTagger);

    @Before
    public void setup() {
        when(speedProfiles.extracted(any(MemoryFeature.class))).thenReturn(Maps.newHashMap());
    }

    @Test
    public void should_add_level_tag() throws Exception {
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0")))).containsEntry("layer", "0");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "1", "T_ELEV", "1")))).containsEntry("layer", "1");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "1")))).containsEntry("layer:from", "0").containsEntry("layer:to", "1");
    }

    @Test
    public void should_tag_roundabout() throws Exception {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "4")))).containsEntry("junction", "roundabout");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "1")))).doesNotContainEntry("junction", "roundabout");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "4")))).containsEntry("junction", "roundabout");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "1")))).doesNotContainEntry("junction", "roundabout");
    }

    @Test
    public void should_tag_ferries() throws Exception {
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "1", "F_ELEV", "0", "T_ELEV", "0", "NAME", "Calais - Douvres", "MINUTES", "10.902")))).containsEntry("route", "ferry")
                .containsEntry("name", "Calais - Douvres").containsEntry("duration", "00:10:54");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "1", "F_ELEV", "0", "T_ELEV", "0", "MINUTES", "10")))).containsEntry("route", "ferry").doesNotContainKey("name");
    }

    @Test
    public void should_tag_ferries_without_cars() throws Exception {
        assertThat(tagger.tag(onlyTags(map("FT", "1", "FEATTYP", "4110", "ID", "123", "MINUTES", "10.902", "F_ELEV", "0", "T_ELEV", "0", "FOW", "14", "NAME", "Calais - Douvres", "ONEWAY", "N"))))
                .containsEntry("route", "ferry").containsEntry("name", "Calais - Douvres").containsEntry("duration", "00:10:54").containsEntry("vehicle", "no");
    }

    @Test
    public void should_tag_vehicle_no() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "ONEWAY", "N")))).containsEntry("vehicle", "no");
    }

    @Test
    public void should_not_tag_vehicle_no_when_restriction_speed() {
        Map<String, String> restrictionsMap = Maps.newHashMap();
        restrictionsMap.put("maxspeed", "60");
        when(speedRestrictionTagger.tag(any(MemoryFeature.class))).thenReturn(restrictionsMap);
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3", "ONEWAY", "N")))).doesNotContainEntry("vehicle", "no");
    }


    @Test
    public void should_tag_pedestrian_roads() {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "14")))).containsEntry("highway", "pedestrian");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "15")))).containsEntry("highway", "footway");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "19")))).containsEntry("highway", "steps");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "3")))).doesNotContainEntry("highway", "steps");
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
    public void should_add_bridge_and_tunnel() throws Exception {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "PARTSTRUC", "0")))).doesNotContainKey("tunnel").doesNotContainKey("bridge");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "PARTSTRUC", "1")))).containsEntry("tunnel", "yes");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "PARTSTRUC", "2")))).containsEntry("bridge", "yes");
    }

    @Test
    public void should_tag_motorway_link() throws Exception {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "10", "FRC", "0"))))
                .containsEntry("highway", "motorway_link");
        assertThat(tagger.tag(onlyTags(map("FT", "0", "FEATTYP", "4110", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "FOW", "10", "FRC", "7"))))
                .containsEntry("highway", "tertiary_link");
    }

    @Test
    public void should_tag_oneway() throws Exception {
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).containsEntry("oneway", "yes");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "TF", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).containsEntry("oneway", "yes");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "N", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).doesNotContainKey("oneway");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).doesNotContainKey("oneway");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("ID", "123", "F_ELEV", "0", "T_ELEV", "0", "FT", "0")))).doesNotContainKey("oneway");
    }

    @Test
    public void should_add_ref_tag() throws Exception {
        assertThat(tagger.tag(onlyTags(map("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0", "SHIELDNUM", "A13")))).containsEntry("ref", "A13");
        assertThat(tagger.tag(onlyTags(ImmutableMap.of("FT", "0", "ID", "123", "MINUTES", "10", "F_ELEV", "0", "T_ELEV", "0")))).doesNotContainKey("ref");
    }
}
