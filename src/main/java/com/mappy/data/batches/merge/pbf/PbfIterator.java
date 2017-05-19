package com.mappy.data.batches.merge.pbf;

import com.mappy.data.batches.utils.PbfBlobDecoder;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.pbf2.v0_6.impl.PbfStreamSplitter;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import static com.google.common.base.Throwables.propagate;
import static com.mappy.data.batches.utils.CollectionUtils.streamIterator;

public class PbfIterator implements Iterator<EntityContainer> {

    private final Iterator<EntityContainer> iterator;

    public PbfIterator(String pbfFile) {
        try {
            PbfStreamSplitter splitter = new PbfStreamSplitter(new DataInputStream(new FileInputStream(pbfFile)));
            iterator = streamIterator(splitter).flatMap(blob -> PbfBlobDecoder.processOsmPrimitives(blob).stream()).iterator();
        }
        catch (FileNotFoundException e) {
            throw propagate(e);
        }
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public EntityContainer next() {
        return iterator.next();
    }
}
