package com.mappy.fpm.batches.utils;

import com.google.inject.Guice;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import org.junit.Test;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfRawBlob;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfStreamSplitter;

import java.io.DataInputStream;
import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;

public class OsmosisSerializerIT {
    @Test
    public void should_generate_building_file() throws Exception {
        Tomtom2Osm launcher = Guice.createInjector(new Tomtom2OsmModule("src/test/resources/osmgenerator/", "target", "target", "rennes")).getInstance(Tomtom2Osm.class);
        launcher.run();

        PbfStreamSplitter splitter = new PbfStreamSplitter(new DataInputStream(new FileInputStream("target/rennes.osm.pbf")));
        assertOSMHeader(splitter);
        assertOSMData(splitter);
    }

    private static void assertOSMHeader(PbfStreamSplitter splitter) throws Exception {
        assertThat(splitter.hasNext()).isTrue();

        PbfRawBlob next = splitter.next();
        assertThat(next.getType()).isEqualTo("OSMHeader");

        HeaderBlock block = HeaderBlock.parseFrom(PbfBlobDecoder.inflate(next));
        assertThat(block.getBbox().getBottom()).isEqualTo(48099668999L);
        assertThat(block.getBbox().getTop()).isEqualTo(48110714999L);
        assertThat(block.getBbox().getLeft()).isEqualTo(-1690166999L);
        assertThat(block.getBbox().getRight()).isEqualTo(-1672390000L);
        assertThat(block.getOsmosisReplicationTimestamp()).isBetween(now().minusMinutes(5).getMillis(), now().plusMinutes(5).getMillis());
    }

    private static void assertOSMData(PbfStreamSplitter splitter) {
        assertThat(splitter.hasNext()).isTrue();
        assertThat(splitter.next().getType()).isEqualTo("OSMData");
    }
}