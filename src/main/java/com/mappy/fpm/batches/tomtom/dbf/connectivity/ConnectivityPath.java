package com.mappy.fpm.batches.tomtom.dbf.connectivity;

import org.jamel.dbf.structure.DbfRow;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectivityPath {
    private final long connectivityId;
    private final int sequenceNumber;
    private final long sectionId;

    public static ConnectivityPath fromDbf(DbfRow entry) {
        return new ConnectivityPath(
                entry.getLong("ID"),
                entry.getInt("SEQNR"),
                entry.getLong("TRPELID")
        );
    }
}
