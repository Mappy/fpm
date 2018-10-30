package com.mappy.fpm.batches.tomtom.dbf.speedrestrictions;

import com.mappy.fpm.utils.MemoryFeature;

import org.junit.Test;

import static com.google.common.collect.ImmutableMap.*;
import static com.google.common.collect.Lists.*;
import static com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.Validity.*;
import static com.mappy.fpm.batches.tomtom.dbf.speedrestrictions.SpeedRestriction.VehicleType;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SpeedRestrictionTaggerTest {
    private final SrDbf dbf = mock(SrDbf.class);
    private final SpeedRestrictionTagger tagger = new SpeedRestrictionTagger(dbf);

    @Test
    public void should_tag_maxspeed() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(new SpeedRestriction(123, 1, 50, both, VehicleType.all)));

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
                new SpeedRestriction(123, 1, 30, positive, VehicleType.all),
                new SpeedRestriction(123, 2, 60, negative, VehicleType.all)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed:forward", "30")
                .containsEntry("maxspeed:backward", "60");
    }

    @Test
    public void should_invert_if_needed() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 1, 30, positive, VehicleType.all),
                new SpeedRestriction(123, 2, 60, negative, VehicleType.all)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123", "ONEWAY", "TF"))))
                .containsEntry("maxspeed:forward", "60")
                .containsEntry("maxspeed:backward", "30");
    }

    @Test
    public void should_handle_both_side() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 1, 80, both, VehicleType.all)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "80");
    }

    @Test
    public void should_handle_multiple_speeds() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 1, 80, both, VehicleType.all),
                new SpeedRestriction(123, 2, 90, both, VehicleType.all)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "90");
    }

    @Test
    public void should_refuse_forbidden_vehicle_types() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 1, 90, both, VehicleType.taxi),
                new SpeedRestriction(123, 2, 80, both, VehicleType.publicBus),
                new SpeedRestriction(123, 3, 70, both, VehicleType.residentialVehicles),
                new SpeedRestriction(123, 4, 60, both, VehicleType.passengerCars)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "60");
    }

    @Test
    public void should_give_priority_to_passenger_cars_vehicle_type() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 1, 90, both, VehicleType.all),
                new SpeedRestriction(123, 2, 80, both, VehicleType.passengerCars)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "80");
    }

    @Test
    public void should_refuse_all_vehicle_type_if_there_is_a_passenger_cars_one() {
        when(dbf.getSpeedRestrictions(123)).thenReturn(newArrayList(
                new SpeedRestriction(123, 1, 80, both, VehicleType.passengerCars),
                new SpeedRestriction(123, 2, 90, both, VehicleType.all)));

        assertThat(tagger.tag(MemoryFeature.onlyTags(of("ID", "123"))))
                .containsEntry("maxspeed", "80");
    }
}
