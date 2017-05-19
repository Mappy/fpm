package com.mappy.data.batches.tomtom;

import com.codahale.metrics.*;
import com.google.common.util.concurrent.*;

import javax.inject.*;

@Singleton
public class TomtomStats {
    private final AtomicLongMap<String> stats = AtomicLongMap.create();
    private final MetricRegistry metrics;

    @Inject
	public TomtomStats(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    public void increment(String key) {
        stats.incrementAndGet(key);
        metrics.counter("com.mappy.data.tomtom." + key).inc();
    }

    public AtomicLongMap<String> getStats() {
        return stats;
    }
}
