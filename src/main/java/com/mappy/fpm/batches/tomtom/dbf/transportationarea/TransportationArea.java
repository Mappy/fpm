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
    private final Long sideOfLine;

    public static TransportationArea fromDbf(DbfRow entry) {
        return new TransportationArea(
                entry.getLong("ID"),
                entry.getInt("TRPELTYP"),
                entry.getLong("AREID"),
                entry.getInt("ARETYP"),
                entry.getLong("SOL"));
    }

    public enum TransportationElementType {
        ROAD_ELEMENT(4110), FERRY_CONNECTION_ELEMENT(4130), ADDRESS_AREA_BOUNDARY_ELEMENT(4165);

        public final Integer code;

        public static Boolean isARoadElement(Integer code){
            return Stream.of(values()).anyMatch(transportationElementType -> code.equals(transportationElementType.code));
        }

        TransportationElementType(Integer code) {
            this.code = code;
        }
    }

    public enum AreaType {
        ADMINISTRATIVE_AREA_ORDER_8(1119),
        ADMINISTRATIVE_AREA_ORDER_9(1120),
        BUILT_UP_AREA(3110);

        public final Integer code;

        AreaType(Integer code) {
            this.code = code;
        }
    }
}
