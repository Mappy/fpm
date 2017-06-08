package com.mappy.fpm.batches.tomtom.dbf.signposts;

import com.google.common.collect.ComparisonChain;
import lombok.Data;
import org.jamel.dbf.structure.DbfRow;

@Data
public class SignPostPath implements Comparable<SignPostPath> {
    private final long id;
    private final int seqnr;
    private final long tomtomId;

    @Override
    public int compareTo(SignPostPath o) {
        return ComparisonChain.start().compare(id, o.getId()).compare(o.getSeqnr(), seqnr).result();
    }

    public static SignPostPath fromRow(DbfRow row) {
        return new SignPostPath(row.getLong("ID"), row.getInt("SEQNR"), row.getLong("TRPELID"));
    }
}
