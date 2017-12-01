package com.mappy.fpm.batches.tomtom.dbf.lanes;

import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static com.mappy.fpm.batches.tomtom.dbf.lanes.Direction.*;
import static org.assertj.core.api.Assertions.*;

public class LaneDirectionTest {
    @Test
    public void should_read_lane_info() {
        assertThat(LaneDirection.parse(65, "R100")).isEqualTo(
                new LaneDirection(newArrayList(Straight, Left), newArrayList(0)));
        assertThat(LaneDirection.parse(129, "R011")).isEqualTo(
                new LaneDirection(newArrayList(Straight, Slight_Left), newArrayList(1, 2)));
    }
}
