package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import com.google.common.collect.ArrayListMultimap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Connectivity {
    private final long connectivityId;
    private final long junctionId;
    private final long originSectionId;
    private final List<Long> destinationSections;
    private final ArrayListMultimap<Integer, Integer> laneMapping;
}
