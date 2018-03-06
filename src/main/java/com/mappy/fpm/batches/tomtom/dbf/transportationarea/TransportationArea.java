package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import lombok.Data;
import org.jamel.dbf.structure.DbfRow;

import java.util.stream.Stream;

@Data
public class TransportationArea {
    private final Long id;
    private final Integer type;
    private final Long areaId;
    private final Integer areaType;
    private final Integer sideOfLine;

    public static TransportationArea fromDbf(DbfRow entry) {
        return new TransportationArea(
                entry.getLong("ID"),
                entry.getInt("TRPELTYP"),
                entry.getLong("AREID"),
                entry.getInt("ARETYP"),
                entry.getInt("SOL"));
    }

    public enum TransportationElementType {
        ROAD_ELEMENT(4110, true),
        FERRY_CONNECTION_ELEMENT(4130, false),
        ADDRESS_AREA_BOUNDARY_ELEMENT(4165, false);

        public final Integer code;
        public final Boolean isARoad;

        public static Boolean isARoadElement(Integer code) {
            return Stream.of(values())
                    .filter(transportationElementType -> transportationElementType.isARoad)
                    .anyMatch(transportationElementType -> code.equals(transportationElementType.code));
        }

        TransportationElementType(Integer code, Boolean isARoad) {
            this.code = code;
            this.isARoad = isARoad;
        }
    }

    public enum AreaType {
        ADMINISTRATIVE_AREA_ORDER_8(1119, false),
        ADMINISTRATIVE_AREA_ORDER_9(1120, false),
        BUILT_UP_AREA(3110, true);

        public final Integer code;
        public final Boolean isABuiltUp;

        public static Boolean isTheMinimumAreaType(Integer code, Boolean needBuiltUp) {
            return Stream.of(values())
                    .filter(areaType1 -> needBuiltUp.equals(areaType1.isABuiltUp))
                    .anyMatch(areaType1 -> code.equals(areaType1.code));
        }

        AreaType(Integer code, Boolean isABuiltUp) {
            this.code = code;
            this.isABuiltUp = isABuiltUp;
        }
    }
}
