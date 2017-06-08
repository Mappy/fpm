package com.mappy.fpm.batches.utils;

import com.github.davidmoten.geo.*;

import static com.google.common.base.Preconditions.checkState;

public class Geohash {
    private static final String symbols = "psc8b9fzejv0uhx1nm5rgt4yk3d627qw";
    private static final int size = symbols.length();
    private static final int[] lookup = indexByLetter(symbols);

    // GeoHashUtils.stringEncode() returns 11 letters.
    // decode() encode letters to long with 5 bit per letter.
    // A geohash takes 55 bits, we use 3 bits to encode the layer
    public static long encodeGeohash(int layer, double x, double y) {
        String stringEncode = GeoHash.encodeHash(y, x, 11);
        long decode = encodeString(stringEncode);
        checkState(layer >= 0 && layer < 8);
        long mask = ((long) layer) << 55;
        return decode | mask;
    }

    public static long encodeString(String s) {
        long num = 0;
        for (char ch : s.toCharArray()) {
            num *= size;
            num += lookup[ch];
        }
        return num;
    }

    public static long withoutLayer(long geohash) {
        long mask = ((long) 0b111) << 55;
        return geohash & ~mask;
    }

    public static long getLayer(long geohash) {
        long mask = ((long) 0b111) << 55;
        return (geohash & mask) >> 55;
    }

    public static String decodeString(long geohash) {
        StringBuilder sb = new StringBuilder();
        while (geohash != 0) {
            sb.append(symbols.charAt((int) (geohash % size)));
            geohash /= size;
        }
        return sb.reverse().toString();
    }

    public static LatLong decodeGeohash(long geohash) {
        return GeoHash.decodeHash(decodeString(withoutLayer(geohash)));
    }

    private static int[] indexByLetter(String symbols) {
        int[] lookup = new int[128];
        int i = 0;
        for (char ch : symbols.toCharArray()) {
            lookup[ch] = i++;
        }
        return lookup;
    }
}