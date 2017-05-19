package com.mappy.data.batches.utils;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.GenericObjectSerializationFactory;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

import java.util.Comparator;
import java.util.Map;

public class NoCompressionEntitySorter implements SinkSource {
    private final FileBasedSort<EntityContainer> fileBasedSort;
    private Sink sink;

    public NoCompressionEntitySorter(Comparator<EntityContainer> comparator) {
        fileBasedSort = new FileBasedSort<>(new GenericObjectSerializationFactory(), comparator);
    }

    public void initialize(Map<String, Object> metaData) {
        sink.initialize(metaData);
    }

    public void process(EntityContainer entityContainer) {
        fileBasedSort.add(entityContainer);
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public void complete() {
        ReleasableIterator<EntityContainer> iterator = null;

        try {
            iterator = fileBasedSort.iterate();

            while (iterator.hasNext()) {
                sink.process(iterator.next());
            }

            sink.complete();
        }
        finally {
            if (iterator != null) {
                iterator.release();
            }
        }
    }

    public void release() {
        fileBasedSort.release();
        sink.release();
    }
}
