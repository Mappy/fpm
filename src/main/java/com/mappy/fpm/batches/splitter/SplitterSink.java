package com.mappy.fpm.batches.splitter;

import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class SplitterSink implements Sink {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final String name;

    protected SplitterSink(String name) {
        this.name = name;
    }

    public void release() {}

    public void complete() {
        log.info("{}: {}", name, counter);
    }

    public void initialize(Map<String, Object> metaData) {}

    public void process(EntityContainer entityContainer) {
        if (entityContainer instanceof NodeContainer) {
            process((NodeContainer) entityContainer);
        }
        else if (entityContainer instanceof WayContainer) {
            process((WayContainer) entityContainer);

        }
        else if (entityContainer instanceof RelationContainer) {
            process((RelationContainer) entityContainer);
        }
        if (counter.getAndIncrement() % 1000000 == 0) {
            log.info("{}: {}", name, counter);
        }
    }

    public abstract void process(NodeContainer node);

    public abstract void process(WayContainer way);

    public abstract void process(RelationContainer rel);
}