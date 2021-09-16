package com.mappy.fpm.batches.tomtom.dbf.routenumbers;

import lombok.Data;
import org.jamel.dbf.structure.DbfRow;

import static java.nio.charset.StandardCharsets.UTF_8;

@Data
public class RouteNumbers {
    private final Long id;
    private final String fullRouteNumber;
    private final Integer RouteNumberType;
    private final Integer RouteNumberPriority;

    public static RouteNumbers fromDbf(DbfRow entry) {
        return new RouteNumbers(
                entry.getLong("ID"),
                entry.getString("SHIELDNUM", UTF_8),
                entry.getInt("RTETYP"),
                entry.getInt("RTEPRIOR")
                );
    }
}
