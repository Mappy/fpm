package com.mappy.fpm.batches.tomtom.dbf.lanes;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;

import org.junit.Test;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LfDbfTest {
    private final TomtomFolder folder = mock(TomtomFolder.class);

    @Test
    public void should_parse_lf_file() {
        when(folder.getFile("lf.dbf")).thenReturn(getClass().getResource("/tomtom/lf.dbf").getPath());
        LfDbf laneTrafficFlow = new LfDbf(folder);

        assertThat(laneTrafficFlow.containsKey(12500000942352L)).isTrue();

        List<Integer> backwardLanes = newArrayList(0);
        List<Integer> forwardLanes = newArrayList(3, 2, 1);
        List<Integer> busLanes = newArrayList(3);
        assertThat(laneTrafficFlow.get(12500000942352L)).containsExactly(
            new LaneTrafficFlow(
                12500000942352L,
                1,  // sequenceNumber
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.all,
                backwardLanes
            ),
            new LaneTrafficFlow(
                12500000942352L,
                2,  // sequenceNumber
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInNegativeDirection,
                VehicleType.all,
                forwardLanes
            ),
            new LaneTrafficFlow(
                12500000942352L,
                3,  // sequenceNumber
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.passengerCars,
                busLanes
            ),
            new LaneTrafficFlow(
                12500000942352L,
                4,  // sequenceNumber
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.residentialVehicles,
                busLanes
            ),
            new LaneTrafficFlow(
                12500000942352L,
                5,  // sequenceNumber
                LaneTrafficFlow.DirectionOfTrafficFlow.closedInPositiveDirection,
                VehicleType.taxi,
                busLanes
            )
        );
    }
}
