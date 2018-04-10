package com.mappy.fpm.batches.tomtom.dbf.geocodes;

import lombok.Data;
import org.jamel.dbf.structure.DbfRow;

import static java.nio.charset.StandardCharsets.UTF_8;

@Data
public class Geocode {
    private final Long id;
    private final Long type;
    private final String name;
    private final String language;
    private final String leftPostalCode;
    private final String rightPostalCode;
    private final Integer leftStructuration;
    private final Integer rightStructuration;
    private final Long sideOfLine;
    private final Long leftFromAdd ;
    private final Long leftToAdd ;
    private final Long rightFromAdd ;
    private final Long rightToAdd ;


    public static Geocode fromDbf(DbfRow entry) {
        return new Geocode(
                entry.getLong("ID"),
                entry.getLong("NAMETYP"),
                entry.getString("FULLNAME", UTF_8),
                entry.getString("NAMELC", UTF_8),
                entry.getString("L_PC", UTF_8),
                entry.getString("R_PC", UTF_8),
                entry.getInt("L_STRUCT"),
                entry.getInt("R_STRUCT"),
                entry.getLong("SOL"),
                entry.getLong("L_F_ADD"),
                entry.getLong("L_T_ADD"),
                entry.getLong("R_F_ADD"),
                entry.getLong("R_T_ADD"));
    }
}
