package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import org.jamel.dbf.structure.DbfRow;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectivityInformation {
    private final long connectivityId;
    private final int originLane;
    private final int destinationLane;
    private final long junctionId;

    public static ConnectivityInformation fromDbf(DbfRow entry) {
        String[] lanes = entry.getString("FROMTO").split("/");
        return new ConnectivityInformation(
                entry.getLong("ID"),
                Integer.parseInt(lanes[0]),
                Integer.parseInt(lanes[1]),
                entry.getLong("JNCTID")
        );
    }
}
