package com.mappy.fpm.batches.tomtom.dbf.lanes;

import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static com.mappy.fpm.batches.tomtom.dbf.lanes.Direction.*;
import static org.assertj.core.api.Assertions.*;

public class LaneTrafficFlowTest {
    @Test
    public void should_read_lane_info() {
        assertThat(LaneTrafficFlow.parseLaneValidity("R001")).isEqualTo(newArrayList(0));
        assertThat(LaneTrafficFlow.parseLaneValidity("R010")).isEqualTo(newArrayList(1));
        assertThat(LaneTrafficFlow.parseLaneValidity("R100")).isEqualTo(newArrayList(2));
        assertThat(LaneTrafficFlow.parseLaneValidity("R110")).isEqualTo(newArrayList(2, 1));
        assertThat(LaneTrafficFlow.parseLaneValidity("R011")).isEqualTo(newArrayList(1, 0));
    }
}
