package com.mappy.fpm.batches.tomtom.dbf.timedomains;

import com.google.common.collect.Ordering;
import lombok.Data;
import org.jetbrains.annotations.NotNull;


@Data
public class TimeDomains implements Comparable<TimeDomains>{

    private final long id;
    private final String domain;

    @Override
    public int compareTo(@NotNull TimeDomains other) {
        return Ordering.natural().compare(this.domain, other.domain);
    }
}
