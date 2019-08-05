package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.mappy.fpm.batches.tomtom.helpers.VehicleType;
import com.mappy.fpm.batches.tomtom.dbf.timedomains.TimeDomains;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.utils.MemoryFeature.onlyTags;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LaneTaggerTest {
    private final LdDbf directions = mock(LdDbf.class);
    private final LfDbf trafficFlow = mock(LfDbf.class);
    private final LtDbf timeDomains = mock(LtDbf.class);
    private final LaneTagger tagger = new LaneTagger(directions, trafficFlow, timeDomains);

    @Test
    public void should_parse_ld_file() {
        when(directions.containsKey(123)).thenReturn(true);
        when(directions.get(123)).thenReturn(newArrayList("slight_left", "through", "slight_right"));

        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "TF", "LANES", "3")), true);

        assertThat(tags).containsEntry("turn:lanes", "slight_left|through|slight_right");
    }

    @Test
    public void should_reverse_if_needed() {
        when(directions.containsKey(123)).thenReturn(true);
        when(directions.get(123)).thenReturn(newArrayList("slight_right", "through", "slight_left"));

        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "3")), false);

        assertThat(tags).containsEntry("turn:lanes", "slight_left|through|slight_right");
    }

    @Test
    public void should_add_number_of_lanes() {
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "3")), false);

        assertThat(tags).containsEntry("lanes", "3");
    }

    @Test
    public void should_properly_tag_bus_lanes() {
        when(trafficFlow.get(123)).thenReturn(newArrayList(
            new LaneTrafficFlow(
                123,
                1,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInNegativeDirection,
                VehicleType.all,
                newArrayList(2, 1, 0)
            ),
            new LaneTrafficFlow(
                123,
                2,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.passengerCars,
                newArrayList(2)
            ),
            new LaneTrafficFlow(
                123,
                3,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.residentialVehicles,
                newArrayList(2)
            ),
            new LaneTrafficFlow(
                123,
                4,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.taxi,
                newArrayList(2)
            )
        ));
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "3")), false);

        assertThat(tags).containsEntry("lanes", "3");
        assertThat(tags).containsEntry("motor_vehicle:lanes:forward", "yes|yes|no");
        assertThat(tags).containsEntry("motor_vehicle:lanes:backward", "no|no|no");
        assertThat(tags).containsEntry("bus:lanes:forward", "yes|yes|designated");
        assertThat(tags).containsEntry("bus:lanes:backward", "no|no|no");
        assertThat(tags).containsEntry("taxi:lanes:forward", "yes|yes|no");
        assertThat(tags).containsEntry("taxi:lanes:backward", "no|no|no");
    }

    @Test
    public void should_properly_tag_private_lanes() {
        when(trafficFlow.get(123)).thenReturn(newArrayList(
            new LaneTrafficFlow(
                123,
                1,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInNegativeDirection,
                VehicleType.all,
                newArrayList(0)
            ),
            new LaneTrafficFlow(
                123,
                2,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.passengerCars,
                newArrayList(0)
            ),
            new LaneTrafficFlow(
                123,
                3,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.publicBus,
                newArrayList(0)
            ),
            new LaneTrafficFlow(
                123,
                4,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.taxi,
                newArrayList(0)
            )
        ));
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "FT", "LANES", "1")), false);

        assertThat(tags).containsEntry("lanes", "1");
        assertThat(tags).containsEntry("motor_vehicle:lanes:forward", "private");
        assertThat(tags).containsEntry("motor_vehicle:lanes:backward", "no");
        assertThat(tags).containsEntry("bus:lanes:forward", "no");
        assertThat(tags).containsEntry("bus:lanes:backward", "no");
        assertThat(tags).containsEntry("taxi:lanes:forward", "no");
        assertThat(tags).containsEntry("taxi:lanes:backward", "no");
    }

    @Test
    public void should_properly_tag_two_way_roads() {
        when(trafficFlow.get(123)).thenReturn(newArrayList(
            new LaneTrafficFlow(
                123,
                1,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInNegativeDirection,
                VehicleType.all,
                newArrayList(3, 2)
            ),
            new LaneTrafficFlow(
                123,
                2,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.all,
                newArrayList(1, 0)
            )
        ));
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "", "LANES", "4")), false);

        assertThat(tags).containsEntry("lanes", "4");
        assertThat(tags).containsEntry("motor_vehicle:lanes:forward", "no|no|yes|yes");
        assertThat(tags).containsEntry("motor_vehicle:lanes:backward", "yes|yes|no|no");
    }

    @Test
    public void should_properly_handle_both_ways_closed_lane() {
        when(trafficFlow.get(123)).thenReturn(newArrayList(
            new LaneTrafficFlow(
                123,
                1,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInBothDirections,
                VehicleType.passengerCars,
                newArrayList(0)
            )
        ));
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "", "LANES", "1")), false);

        assertThat(tags).containsEntry("lanes", "1");
        assertThat(tags).containsEntry("motor_vehicle:lanes:forward", "private");
        assertThat(tags).containsEntry("motor_vehicle:lanes:backward", "private");
    }

    @Test
    public void should_consider_restriction_time_domains() {
        when(trafficFlow.get(123)).thenReturn(newArrayList(
            new LaneTrafficFlow(
                123,
                1,
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInBothDirections,
                VehicleType.passengerCars,
                newArrayList(0)
            )
        ));
        when(timeDomains.getTimeDomains(
            LtDbf.RestrictionType.directionOfTrafficFlow,
            123,
            1
        )).thenReturn(new TimeDomains(123, 1, ""));
        Map<String, String> tags = tagger.lanesFor(onlyTags(ImmutableMap.of("ID", "123", "ONEWAY", "", "LANES", "1")), false);

        assertThat(tags).containsEntry("lanes", "1");
        assertThat(tags).containsEntry("motor_vehicle:lanes:forward", "yes");
        assertThat(tags).containsEntry("motor_vehicle:lanes:backward", "yes");
    }
}
