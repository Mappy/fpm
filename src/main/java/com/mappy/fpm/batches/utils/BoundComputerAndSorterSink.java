package com.mappy.fpm.batches.utils;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityContainerComparator;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBBox;
import org.openstreetmap.osmosis.osmbinary.Osmformat.HeaderBBox.Builder;

import java.util.Map;

public class BoundComputerAndSorterSink implements Sink {
    private boolean initialized;
    private double top;
    private double bottom;
    private double left;
    private double right;

    private final Sink sorter;
    private final PbfSink pbf;

    public BoundComputerAndSorterSink(PbfSink pbf) {
        this.pbf = pbf;
        NoCompressionEntitySorter sorter = new NoCompressionEntitySorter(new EntityContainerComparator(new EntityByTypeThenIdComparator()));
        sorter.setSink(pbf);
        this.sorter = sorter;
    }

    @Override
    public void initialize(Map<String, Object> metaData) {
        sorter.initialize(metaData);
    }

    @Override
    public void complete() {
        pbf.writeHeader(bbox());
        sorter.complete();
    }

    @Override
    public void release() {
        sorter.release();
    }

    @Override
    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof NodeContainer) {
            Node node = ((NodeContainer) entityContainer).getEntity();
            if (initialized) {
                left = Math.min(left, node.getLongitude());
                right = Math.max(right, node.getLongitude());
                bottom = Math.min(bottom, node.getLatitude());
                top = Math.max(top, node.getLatitude());
            }
            else {
                left = node.getLongitude();
                right = node.getLongitude();
                top = node.getLatitude();
                bottom = node.getLatitude();
                initialized = true;
            }
        }
        sorter.process(entityContainer);
    }

    private HeaderBBox bbox() {
        Builder bbox = HeaderBBox.newBuilder();
        bbox.setLeft(mapRawDegrees(left));
        bbox.setBottom(mapRawDegrees(bottom));
        bbox.setRight(mapRawDegrees(right));
        bbox.setTop(mapRawDegrees(top));
        return bbox.build();
    }

    private static long mapRawDegrees(double degrees) {
        return (long) ((degrees / .000000001));
    }
}