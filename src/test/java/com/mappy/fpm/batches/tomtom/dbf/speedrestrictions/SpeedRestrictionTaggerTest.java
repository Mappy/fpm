package com.mappy.fpm.batches.tomtom.dbf.speedrestrictions;

import com.mappy.fpm.utils.MemoryFeature;

import org.junit.Test;

import static com.google.common.collect.ImmutableMap.*;
import static com.google.common.collect.Lists.*;
import static com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.Validity.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SpeedRestrictionTaggerTest {
    private final SrDbf dbf = mock(SrDbf.class);
    private final SpeedRestrictionTagger tagger = new SpeedRestrictionTagger(dbf);

    @Test
    public void should_tag_maxspeed() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(new SpeedRestriction(123, 50, both)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "50");
    }

    @Test
    public void should_not_add_maxspeed_if_not_present_in_dbf() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList());

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123")))).isEmpty();
    }

    @Test
    public void should_tag_maxspeed_for_each_side() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 30, positive),
                new SpeedRestriction(123, 60, negative)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed:forward", "30")
                .containsEntry("maxspeed:backward", "60");
    }

    @Test
    public void should_invert_if_needed() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 30, positive),
                new SpeedRestriction(123, 60, negative)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123", "ONEWAY", "TF"))))
                .containsEntry("maxspeed:forward", "60")
                .containsEntry("maxspeed:backward", "30");
    }

    @Test
    public void should_handle_both_side() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 80, both)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "80");
    }

    @Test
    public void should_handle_multiple_speeds() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 80, both),
                new SpeedRestriction(123, 90, both)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "80");
    }
}
