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
    private final Long sideOfLine;

    public static AlternativeName fromDbf(DbfRow entry, String alternativeParamName, boolean hasSideNames) {
        return new AlternativeName(
                entry.getLong("ID"),
                !hasSideNames ? entry.getString("NAMETYP") : null,
                entry.getString(alternativeParamName, UTF_8),
                entry.getString("NAMELC", UTF_8),
                hasSideNames ? entry.getLong("SOL") : null);
    }
}
