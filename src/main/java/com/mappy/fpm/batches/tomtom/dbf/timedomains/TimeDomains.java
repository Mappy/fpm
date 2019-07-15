package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.google.common.collect.Ordering;
import lombok.Data;
import org.jamel.dbf.structure.DbfRow;
import org.jetbrains.annotations.NotNull;

import static java.nio.charset.StandardCharsets.UTF_8;

@Data
public class TimeDomains implements Comparable<TimeDomains>{

    private final long id;
    private final int sequenceNumber;
    private final String domain;

    @Override
    public int compareTo(@NotNull TimeDomains other) {
        return Ordering.natural().compare(this.domain, other.domain);
    }

    public static TimeDomains fromDbf(DbfRow entry) {
        return new TimeDomains(
                entry.getLong("ID"),
                entry.getInt("SEQNR"),
                entry.getString("TIMEDOM", UTF_8).trim()
        );
    }
}
