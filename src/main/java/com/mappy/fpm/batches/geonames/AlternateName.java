package com.mappy.fpm.batches.geonames;

import com.google.common.collect.ComparisonChain;
import lombok.Data;

import java.util.BitSet;

@Data
public class AlternateName implements Comparable<AlternateName> {
    private final String value;
    private final BitSet mask;

    public AlternateName(String value, boolean isPreferredName, boolean isShortName, boolean isColloquial, boolean isHistoric) {
        this.value = value;
        this.mask = mask(isPreferredName, isShortName, isColloquial, isHistoric);
    }

    public boolean isPreferredName() {
        return mask.get(0);
    }

    public boolean isShortName() {
        return mask.get(1);
    }

    public boolean isColloquial() {
        return mask.get(2);
    }

    public boolean isHistoric() {
        return mask.get(3);
    }

    private static BitSet mask(boolean isPreferredName, boolean isShortName, boolean isColloquial, boolean isHistoric) {
        BitSet bitSet = new BitSet(4);
        bitSet.set(0, isPreferredName);
        bitSet.set(1, isShortName);
        bitSet.set(2, isColloquial);
        bitSet.set(3, isHistoric);
        return bitSet;
    }

    @Override
    public int compareTo(AlternateName other) {
        return ComparisonChain.start()//
                .compareTrueFirst(isPreferredName(), other.isPreferredName())//
                .compareFalseFirst(isHistoric(), other.isHistoric())//
                .compareFalseFirst(isShortName(), other.isShortName())//
                .compareFalseFirst(isColloquial(), other.isColloquial())//
                .compare(value, other.getValue()).result();
    }
}