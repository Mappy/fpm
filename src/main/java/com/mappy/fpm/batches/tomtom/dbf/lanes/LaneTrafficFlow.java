package com.mappy.fpm.batches.tomtom.dbf.lanes;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.mappy.fpm.batches.tomtom.helpers.VehicleType;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

import org.jamel.dbf.exception.DbfException;
import org.jamel.dbf.structure.DbfRow;

import static java.util.stream.Collectors.toList;

@Data
@AllArgsConstructor
public class LaneTrafficFlow {
    private final long id;
    private final int sequenceNumber;
    private final DirectionOfTrafficFlow direction;
    private final VehicleType vehicleType;
    private final List<Integer> laneValidity;

    public enum Direction {
        forward("forward"),
        backward("backward");

        public final String osmLabel;

        private Direction(String label) {
            osmLabel = label;
        }
    }

    public enum DirectionOfTrafficFlow {
        openInBothDirections(EnumSet.noneOf(Direction.class)),
        closedInPositiveDirection(EnumSet.of(Direction.forward)),
        closedInNegativeDirection(EnumSet.of(Direction.backward)),
        closedInBothDirections(EnumSet.allOf(Direction.class));

        public final EnumSet<Direction> directions;

        private DirectionOfTrafficFlow(EnumSet<Direction> directions) {
            this.directions = directions;
        }
    }

    public static List<Integer> parseLaneValidity(String input) {
        return IntStream.range(1, input.length()).filter(
            i -> input.charAt(i) == '1'
        ).map(
            // define lanes left to right
            // Tomtom defines them right to left
            // minus 1 to account for the leading "R"
            // example value "R0011"
            i -> (input.length() - i - 1)
        ).boxed().collect(toList());
    }

    public static LaneTrafficFlow fromRow(DbfRow row) {
        return new LaneTrafficFlow(
            row.getLong("ID"),
            row.getInt("SEQNR"),
            DirectionOfTrafficFlow.values()[row.getInt("DFLANE") - 1],
            VehicleType.fromId(row.getInt("VT")),
            parseLaneValidity(row.getString("VALIDITY"))
        );
    }
}
