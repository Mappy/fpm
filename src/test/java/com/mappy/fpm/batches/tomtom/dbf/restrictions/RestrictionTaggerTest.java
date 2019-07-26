package com.mappy.fpm.batches.tomtom.dbf.restrictions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsParser;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomainsProvider;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;
import com.mappy.fpm.utils.MemoryFeature;

import org.junit.Test;

public class RestrictionTaggerTest {
    private final RsDbf rsDbf = mock(RsDbf.class);
    private final TimeDomainsProvider timeDomainsProvider = mock(TimeDomainsProvider.class);
    private final TimeDomainsParser timeDomainsParser = new TimeDomainsParser();
    private final RestrictionTagger restrictionTagger = new RestrictionTagger(rsDbf, timeDomainsProvider,
            timeDomainsParser);

    @Test
    public void should_tag_two_way_streets_properly() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList());
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .isEmpty();
    }

    @Test
    public void should_tag_one_way_streets_properly() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inNegativeLineDirection, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.all)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .containsOnly(entry("oneway", "yes"));
    }

    @Test
    public void should_tag_one_way_reverse_streets_properly() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inPositiveLineDirection, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.all)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .containsOnly(entry("oneway", "yes"), entry("reversed:tomtom", "yes"));
    }

    @Test
    public void should_tag_closed_streets_properly() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.all)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .containsOnly(entry("motor_vehicle", "no"));
    }

    @Test
    public void should_consider_passenger_cars_restrictions() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.passengerCars)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .containsOnly(entry("motor_vehicle", "no"));
    }

    @Test
    public void should_ignore_residential_cars_restrictions() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.residentialVehicles)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .isEmpty();
    }

    @Test
    public void should_ignore_taxis_restrictions() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.taxi)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .isEmpty();
    }

    @Test
    public void should_ignore_buses_restrictions() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.publicBus)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .isEmpty();
    }

    @Test
    public void should_ignore_low_emissions_restrictions() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inBothLineDirections, Restriction.Type.lowEmissionRestrictionType, 0, VehicleType.all)
        ));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(null);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .isEmpty();
    }

    @Test
    public void should_consider_time_domains_for_restrictions() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inPositiveLineDirection, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.all),
            new Restriction(123, 2, Restriction.Validity.inNegativeLineDirection, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.all)
        ));
        ArrayListMultimap<Integer, TimeDomains> timeDomains = ArrayListMultimap.create();
        timeDomains.put(1, new TimeDomains(123, 1, "[(h11){h7}]"));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(timeDomains);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .containsOnly(entry("oneway", "yes"), entry("opening_hours", "11:00-18:00 off"));
    }

    @Test
    public void should_merge_time_domains() {
        when(rsDbf.getRestrictions(123)).thenReturn(Lists.newArrayList(
            new Restriction(123, 1, Restriction.Validity.inPositiveLineDirection, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.all),
            new Restriction(123, 2, Restriction.Validity.inNegativeLineDirection, Restriction.Type.directionOfTrafficFlow, 0, VehicleType.all)
        ));
        ArrayListMultimap<Integer, TimeDomains> timeDomains = ArrayListMultimap.create();
        timeDomains.put(1, new TimeDomains(123, 1, "[(h11){h7}]"));
        timeDomains.put(2, new TimeDomains(123, 1, "[(h22){h8}]"));
        when(timeDomainsProvider.getSectionTimeDomains(123)).thenReturn(timeDomains);
        assertThat(restrictionTagger.tag(MemoryFeature.onlyTags(ImmutableMap.of("ID", "123"))))
                .containsOnly(entry("opening_hours", "22:00-06:00 off, 11:00-18:00 off"));
    }
}
