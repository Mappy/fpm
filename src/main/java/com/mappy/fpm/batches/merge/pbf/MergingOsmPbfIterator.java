package com.mappy.fpm.batches.merge.pbf;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.*;

@Slf4j
public class MergingOsmPbfIterator implements Iterator<EntityContainer> {

    private final Iterator<EntityContainer> it1;
    private final Iterator<EntityContainer> it2;
    private EntityMergeResult next = new EntityMergeResult();
    private OsmPbfStats stats;

    public MergingOsmPbfIterator(Iterator<EntityContainer> it1, Iterator<EntityContainer> it2) {
        this.it1 = it1;
        this.it2 = it2;
    }

    @Override
    public boolean hasNext() {
        if (next.getResult() != null) {
            return true;
        }
        if (next.getEc1() == null) {
            next.setEc1(it1.hasNext() ? it1.next() : null);
        }
        if (next.getEc2() == null) {
            next.setEc2(it2.hasNext() ? it2.next() : null);
        }
        next = next.merge();
        boolean result = next.getResult() != null;
        if (!result && stats != null) {
            stats.display();
            stats = null;
        }
        return result;
    }

    @Override
    public EntityContainer next() {
        if (next.getResult() == null) {
            Preconditions.checkState(hasNext(), "cannot call next, no more elements in terator");
        }
        EntityContainer result = next.getResult();
        next.setResult(null);
        if (stats != null) stats.next(result.getEntity(), next.isMerged());
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Iterator<EntityContainer> merge(Iterator<EntityContainer>... iterators) {
        Iterator<EntityContainer> result = iterators[0];
        for (Iterator<EntityContainer> iter : iterators) {
            if (iter != result) result = new MergingOsmPbfIterator(iter, result);
        }
        ((MergingOsmPbfIterator) result).logStats();
        return result;
    }

    public void logStats() {
        this.stats = new OsmPbfStats();
    }

    private static class OsmPbfStats {
        private final Map<EntityType, AtomicInteger> stats = ImmutableMap.of(Bound, new AtomicInteger(), Node, new AtomicInteger(), Way, new AtomicInteger(), Relation, new AtomicInteger());
        private final Map<EntityType, AtomicInteger> mergedStats = ImmutableMap.of(Bound, new AtomicInteger(), Node, new AtomicInteger(), Way, new AtomicInteger(), Relation, new AtomicInteger());
        private EntityType currentType = Bound;
        private final Stopwatch watch = Stopwatch.createStarted();

        public void next(Entity entity, boolean merged) {
            if (entity.getType() != currentType) {
                display();
                watch.reset();
                watch.start();
                currentType = entity.getType();
            }
            stats.get(currentType).incrementAndGet();
            if (merged) mergedStats.get(currentType).incrementAndGet();
        }

        public void display() {
            log.info("Done processing {} {} ({} merged) in {}", stats.get(currentType).get(), currentType, mergedStats.get(currentType).get(), watch);
        }
    }
}
