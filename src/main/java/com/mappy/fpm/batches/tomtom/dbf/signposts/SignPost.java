package com.mappy.fpm.batches.tomtom.dbf.signposts;

import com.google.common.collect.ComparisonChain;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jamel.dbf.structure.DbfRow;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

@Data
@EqualsAndHashCode(of = "txtcont")
public class SignPost implements Comparable<SignPost> {

    private final long id;
    private final int seqnr;
    private final int destseq;
    private final InfoType infotyp;
    private final String txtcont;
    private final String txtcontlc;
    private final ConnectionType contyp;

    public static SignPost fromRow(DbfRow row) {
        return new SignPost(row.getLong("ID"),
                row.getInt("SEQNR"),
                row.getInt("DESTSEQ"),
                InfoType.byCode.get(row.getString("INFOTYP")),
                row.getString("TXTCONT"),
                row.getString("TXTCONTLC"),
                ConnectionType.values()[row.getInt("CONTYP")]);
    }

    @Override
    public int compareTo(SignPost o) {
        return ComparisonChain.start().compare(id, o.getId()).compare(seqnr, o.getSeqnr()).compare(destseq, o.getDestseq()).result();
    }

    public enum ConnectionType {
        Undefined, Branch, Towards, Exit
    }

    @Getter
    public enum PictogramType {
        airport("airport"), bus_station("bus_station"), fair("fair"), ferry_connection("ferry"), first_aid_post("hospital"), harbour("harbour"), hospital("hospital"), hotel_motel("lodging"), industrial_area("industrial"), information_center("info"), parking_facility("parking"), petrol_station("fuel"), railway_station("train_station"), rest_area("food"), restaurant("food"), toilet("toilets");

        public final String osmCode;

        PictogramType(String osmCode) {
            this.osmCode = osmCode;
        }

        public static String name(String pos) {
            return Arrays.asList(PictogramType.values()).get(Integer.valueOf(pos) - 1).getOsmCode();
        }
    }
}
