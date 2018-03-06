package com.mappy.fpm.batches.tomtom.dbf.names;

import lombok.Data;

import org.jamel.dbf.structure.DbfRow;

import static java.nio.charset.StandardCharsets.*;

@Data
public class AlternativeName {

    private final Long id;
    private final String type;
    private final String name;
    private final String language;

    public static AlternativeName fromDbf(DbfRow entry) {
        return new AlternativeName(
                entry.getLong("ID"),
                entry.getString("NAMETYP"),
                entry.getString("NAME", UTF_8),
                entry.getString("NAMELC", UTF_8));
    }
}
