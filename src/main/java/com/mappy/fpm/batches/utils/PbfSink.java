package com.mappy.fpm.batches.utils;

import crosby.binary.osmosis.OsmosisSerializer;
import lombok.experimental.Delegate;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBBox;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBlock;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import java.io.OutputStream;
import java.util.Date;

public class PbfSink implements Sink {
    @Delegate
    private final OsmosisSerializer serializer;

    public PbfSink(OutputStream output, boolean compress) {
        BlockOutputStream os = new BlockOutputStream(output);
        if (!compress) {
            os.setCompress("none");
        }
        serializer = new OsmosisSerializer(os);
    }

    public void writeHeader(HeaderBBox bbox) {
        serializer.finishHeader(HeaderBlock.newBuilder()
                .setBbox(bbox)
                .setSource("fpm")
                .setOsmosisReplicationTimestamp(new Date().getTime()));
    }
}