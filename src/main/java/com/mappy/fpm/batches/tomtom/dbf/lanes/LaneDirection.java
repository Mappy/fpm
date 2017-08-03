package com.mappy.fpm.batches.tomtom.dbf.lanes;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.*;
import static java.util.stream.Collectors.*;

@Data
@AllArgsConstructor
public class LaneDirection {
    private final List<Direction> directions;
    private final List<Integer> validity;

    public static LaneDirection parse(int direction, String input) {
        return new LaneDirection(parseDirection(direction), parseValidity(input));
    }

    private static List<Direction> parseDirection(int direction) {
        List<Direction> dirs = newArrayList();
        for (Direction dir : Direction.values()) {
            if ((direction & 1 << dir.mask) != 0) {
                dirs.add(dir);
            }
        }
        return dirs;
    }

    private static List<Integer> parseValidity(String input) {
        return IntStream.range(1, input.length()).filter(i -> input.charAt(i) == '1').map(i -> i - 1).boxed().collect(toList());
    }
}