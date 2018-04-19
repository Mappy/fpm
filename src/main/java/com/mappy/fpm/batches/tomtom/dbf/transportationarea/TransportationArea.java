package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import lombok.Data;
import org.jamel.dbf.structure.DbfRow;

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

}
