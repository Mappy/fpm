package com.mappy.fpm.batches.merge;

import com.mappy.fpm.batches.naturalearth.discarded.CoastLinesShapefile;
import com.mappy.fpm.batches.utils.LargePolygonSplitter;
import com.mappy.fpm.batches.utils.ShapefileIterator;
import com.mappy.fpm.batches.utils.ShapefileWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory.Builder;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Throwables.propagate;
import static java.util.stream.Collectors.toList;

@Slf4j
public class Oceans {
    private static final double DELTA = 0.0000001;

    public void generate(File landsFile) {
        STRtree lands = lands(landsFile);

        List<Geometry> world = splittedWorld(lands);
        List<Geometry> oceans = oceans(lands, world);
        ShapefileWriter.write(new File("/workspace/oceans.shp"), oceans, MultiPolygon.class);
    }

    private static List<Geometry> oceans(STRtree tree, List<Geometry> world) {
        BasicThreadFactory threadFactory = new Builder().namingPattern("mappy-Oceans-%d").daemon(false).build();
        ExecutorService threadPool = Executors.newFixedThreadPool(6, threadFactory);
        try {
            AtomicInteger counter = new AtomicInteger(world.size());
            List<Future<Geometry>> oceans = world.stream().map(g -> threadPool.submit(() -> difference(tree, counter, g))).collect(toList());
            return oceans.stream().map(future -> {
                try {
                    return future.get();
                }
                catch (InterruptedException|ExecutionException e) {
                    throw propagate(e);
                }
            }).collect(toList());
        }
        finally {
            threadPool.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Geometry> splittedWorld(STRtree tree) {
        return LargePolygonSplitter.split(CoastLinesShapefile.world(), 5, g -> {
            List<Geometry> query = tree.query(g.getEnvelopeInternal());
            return query.stream().mapToInt(Geometry::getNumPoints).sum() < 2000;
        });
    }

    private static STRtree lands(File file) {
        STRtree tree = new STRtree();
        try (ShapefileIterator iterator = new ShapefileIterator(file, true)) {
            iterator.forEachRemaining(feature -> {
                for (Polygon polygon : PolygonsUtils.polygons(feature.getGeometry())) {
                    for (Geometry p : LargePolygonSplitter.split(polygon, 30)) {
                        tree.insert(p.getEnvelopeInternal(), p);
                    }
                }
            });
        }
        return tree;
    }

    @SuppressWarnings("unchecked")
    private static Geometry difference(STRtree tree, AtomicInteger counter, Geometry geom) {
        List<Geometry> query = tree.query(geom.getEnvelopeInternal());
        log.info("{} {} {}", counter.getAndDecrement(), query.size(), geom.getEnvelopeInternal());
        if (!query.isEmpty()) {
            List<Geometry> collect = query.stream().map(t -> t.buffer(DELTA)).collect(toList());
            return geom.difference(CascadedPolygonUnion.union(collect).buffer(-1 * DELTA));
        }
        return geom;
    }
}
