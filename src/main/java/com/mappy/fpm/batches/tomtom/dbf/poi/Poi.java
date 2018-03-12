package com.mappy.fpm.batches.tomtom.dbf.poi;

import lombok.Data;
import org.jamel.dbf.structure.DbfRow;

import static java.nio.charset.StandardCharsets.UTF_8;

@Data
public class Poi {

    private final Long id;
    private final String name;


    public static Poi fromDbf(DbfRow entry) {
        return new Poi(
                entry.getLong("ID"),
                entry.getString("NAME", UTF_8));
    }
}