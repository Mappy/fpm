package com.mappy.data.batches.utils;

import com.github.davidmoten.geo.LatLong;

import org.junit.Test;

import static com.mappy.data.batches.utils.Geohash.*;
import static org.assertj.core.api.Assertions.*;

public class GeohashTest {
    @Test
    public void should_encode_point() throws Exception {
        assertThat(encodeGeohash(0, 1.0, 1.0)).isEqualTo(1525778252023487L);
        assertThat(encodeGeohash(1, 1.0, 1.0)).isEqualTo(37554575270987455L);
        assertThat(encodeGeohash(2, 1.0, 1.0)).isEqualTo(73583372289951423L);
        assertThat(encodeGeohash(3, 1.0, 1.0)).isEqualTo(109612169308915391L);
    }

    @Test
    public void should_remove_layer() throws Exception {
        assertThat(withoutLayer(encodeGeohash(1, 1.0, 1.0))).isEqualTo(encodeGeohash(0, 1.0, 1.0));
        assertThat(withoutLayer(encodeGeohash(5, 3.0, 48.0))).isEqualTo(encodeGeohash(0, 3.0, 48.0));
    }

    @Test
    public void should_return_layer() throws Exception {
        assertThat(Geohash.getLayer(encodeGeohash(5, 3.0, 48.0))).isEqualTo(5);
        assertThat(Geohash.getLayer(encodeGeohash(0, 1.0, 1.0))).isEqualTo(0);
        assertThat(Geohash.getLayer(encodeGeohash(2, 4.0, 55.0))).isEqualTo(2);
    }

    @Test
    public void should_encode_and_decode_geohash() throws Exception {
        assertThat(decodeString(encodeString("u0d1h30d"))).isEqualTo("u0d1h30d");

        assertThat(decodeString(encodeGeohash(0, 1.0, 1.0))).isEqualTo("s00twy01mtw");
        assertThat(decodeString(encodeGeohash(0, 3.0, 48.0))).isEqualTo("u0d1h60s30d");
    }

    @Test
    public void should_decode_geohash() throws Exception {
        LatLong decode = Geohash.decodeGeohash(encodeGeohash(0, 2.294351, 48.858844));

        assertThat(decode.getLat()).isCloseTo(48.858844, within(0.000001));
        assertThat(decode.getLon()).isCloseTo(2.294351, within(0.000001));
    }
}
