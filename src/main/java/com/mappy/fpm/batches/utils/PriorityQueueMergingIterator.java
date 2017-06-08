package com.mappy.fpm.batches.utils;

// This software is released into the Public Domain.  See copying.txt for details.

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 * This iterator examines a list of sorted input sources and merges them into a single sorted list.
 * 
 * @param <DataType>
 *            The object type to be sorted.
 * @author Brett Henderson
 */
public class PriorityQueueMergingIterator<DataType> implements ReleasableIterator<DataType> {
    private final List<ReleasableIterator<DataType>> sources;
    private final Comparator<DataType> comparator;
    private List<DataType> sourceData;
    private PriorityQueue<QueueElement<DataType>> queue;

    @Data
    @AllArgsConstructor
    public static class QueueElement<DataType> {
        private DataType data;
        private final int source;
    }

    /**
     * Creates a new instance.
     * 
     * @param sources
     *            The list of data sources.
     * @param comparator
     *            The comparator to be used for sorting.
     */
    public PriorityQueueMergingIterator(List<ReleasableIterator<DataType>> sources, Comparator<DataType> comparator) {
        this.sources = new ArrayList<>(sources);
        this.comparator = comparator;
    }

    /**
     * Primes the sorting collections.
     */
    private void initialize() {
        if (sourceData == null) {
            // Get the first entity from each source. Delete any empty sources.
            sourceData = new ArrayList<>(sources.size());
            queue = new PriorityQueue<>(Comparator.comparing(QueueElement::getData, comparator));
            for (int sourceIndex = 0; sourceIndex < sources.size();) {
                ReleasableIterator<DataType> source;

                source = sources.get(sourceIndex);

                if (source.hasNext()) {
                    DataType next = source.next();
                    sourceData.add(next);
                    queue.add(new QueueElement<>(next, sourceIndex));
                    sourceIndex++;
                }
                else {
                    sources.remove(sourceIndex).release();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        initialize();

        return !queue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public DataType next() {
        DataType dataMinimum;
        int indexMinimum;
        ReleasableIterator<DataType> source;

        initialize();

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        QueueElement<DataType> peek = queue.poll();
        dataMinimum = peek.getData();
        indexMinimum = peek.getSource();

        // Get the next entity from the source if available.
        // Otherwise remove the source and its current data.
        source = sources.get(indexMinimum);
        if (source.hasNext()) {
            DataType next = source.next();
            peek.setData(next);
            queue.add(peek);
        }
        else {
            source.release();
        }

        return dataMinimum;
    }

    /**
     * Not supported. An UnsupportedOperationException is always thrown.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public void release() {
        for (ReleasableIterator<DataType> source : sources) {
            source.release();
        }
    }
}
