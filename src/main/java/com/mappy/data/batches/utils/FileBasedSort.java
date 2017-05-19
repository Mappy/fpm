package com.mappy.data.batches.utils;

//This software is released into the Public Domain.  See copying.txt for details.

import org.openstreetmap.osmosis.core.lifecycle.Releasable;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.ChunkedObjectStore;
import org.openstreetmap.osmosis.core.store.ObjectSerializationFactory;
import org.openstreetmap.osmosis.core.store.Storeable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class FileBasedSort<T extends Storeable> implements Releasable {
    private static final int MAX_MEMORY_SORT_COUNT = 16384;

    private final Comparator<T> comparator;
    private final ChunkedObjectStore<T> chunkedEntityStore;
    private final List<T> addBuffer;

    /**
     * Creates a new instance.
     * 
     * @param serializationFactory
     *            The factory defining the object serialisation implementation.
     * @param comparator
     *            The comparator to be used for sorting the results.
     */
    public FileBasedSort(ObjectSerializationFactory serializationFactory, Comparator<T> comparator) {
        this.comparator = comparator;
        chunkedEntityStore = new ChunkedObjectStore<>(serializationFactory, "emta", "idx", false);
        addBuffer = new ArrayList<>(MAX_MEMORY_SORT_COUNT);
    }

    /**
     * Sorts the data currently in the add buffer, writes it to the object store, and clears the
     * buffer.
     */
    private void flushAddBuffer() {
        if (addBuffer.size() >= 0) {
            // Sort the chunk prior to writing.
            addBuffer.sort(comparator);

            // Write all entities in the buffer to entity storage.
            for (T entity : addBuffer) {
                chunkedEntityStore.add(entity);
            }

            addBuffer.clear();

            // Close the chunk in the underlying data store so that it can be read separately.
            chunkedEntityStore.closeChunk();
        }
    }

    /**
     * Adds a new object to be sorted.
     * 
     * @param value
     *            The data object.
     */
    public void add(T value) {
        // Add the new data entity to the add buffer.
        addBuffer.add(value);

        // If the add buffer is full, it must be sorted and written to entity
        // storage.
        if (addBuffer.size() >= MAX_MEMORY_SORT_COUNT) {
            flushAddBuffer();
        }
    }

    /**
     * Sorts the specified sub-section of the overall storage contents. This result list is not
     * backed by a file and should be persisted prior to being incorporated into a higher level
     * merge operation.
     * 
     * @param chunkCount
     *            The number of chunks to sort.
     * @return An iterator providing access to the sort result.
     */
    private ReleasableIterator<T> iterate(long chunkCount) {
        List<ReleasableIterator<T>> sources;

        sources = newArrayList();

        try {
            PriorityQueueMergingIterator<T> mergingIterator;

            // If we are down to a small number of entities, we retrieve each
            // source from file.
            // Otherwise we recurse and split the number of entities down into
            // smaller chunks.
            for (int i = 0; i < chunkCount; i++) {
                sources.add(chunkedEntityStore.iterate(i));
            }

            // Create a merging iterator to merge all of the sources.
            mergingIterator = new PriorityQueueMergingIterator<>(sources, comparator);

            // The merging iterator owns the sources now, so we clear our copy
            // of them to prevent them being released on method exit.
            sources.clear();

            return mergingIterator;

        }
        finally {
            for (ReleasableIterator<T> source : sources) {
                source.release();
            }
        }
    }

    /**
     * Sorts and returns the contents of the sorter.
     * 
     * @return An iterator providing access to the sorted entities.
     */
    public ReleasableIterator<T> iterate() {
        flushAddBuffer();

        return iterate(chunkedEntityStore.getChunkCount());
    }

    /**
     * {@inheritDoc}
     */
    public void release() {
        chunkedEntityStore.release();
    }
}
