package com.mappy.data.batches.tomtom.dbf.maneuvers;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Restriction {
    private final List<Long> segments;
    private final Long junctionId;
}