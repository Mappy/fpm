package com.mappy.fpm.batches.splitter;

import com.google.common.collect.Lists;
import com.mappy.fpm.batches.utils.ShapefileWriter;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.strtree.STRtree;
import crosby.binary.osmosis.OsmosisSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Throwables.propagate;
import static com.mappy.fpm.batches.merge.NaturalEarthWorld.world;

@Slf4j
public class SplitterShapefileGenerator {

    private static final GeometryFactory gf = new GeometryFactory();

    public static void main(String[] args) {
        ShapefileWriter.write(new File("/tmp/split.shp"), subdivide(world(), 0, "/tmp/Europe.osm.pbf"), MultiPolygon.class);
    }

    @Data
    public static class Step {
        private final Geometry geom;
        private final AtomicInteger counter = new AtomicInteger(0);
        private final File filename;
        private final OsmosisSerializer serializer;

        public Step(Geometry geom) {
            try {
                this.geom = geom;
                filename = new File("/tmp/pbfs", UUID.randomUUID().toString());
                BlockOutputStream os;
                os = new BlockOutputStream(new FileOutputStream(filename));
                os.setCompress("none");
                serializer = new OsmosisSerializer(os);
            }
            catch (FileNotFoundException e) {
                throw propagate(e);
            }
        }
    }

    private static List<Geometry> subdivide(Geometry world, int depth, String source) {
        Point centroid = world.getCentroid();
        STRtree tree = new STRtree();
        for (int i = 0; i < 4; i++) {
            Geometry box = box(centroid, world.getCoordinates()[i]);
            tree.insert(box.getEnvelopeInternal(), new Step(box));
        }
        log.info("{}: {}", depth, tree.itemsTree());
        PbfReader reader = new PbfReader(new File(source), 2);
        AtomicInteger integer = new AtomicInteger(0);
        reader.setSink(new Sink() {
            @Override
            public void release() {}

            @Override
            public void complete() {}

            @Override
            public void initialize(Map<String, Object> metaData) {}

            @Override
            public void process(EntityContainer entityContainer) {
                if (entityContainer instanceof NodeContainer && (depth > 0 || integer.getAndIncrement() % 4 == 0)) {
                    Node node = ((NodeContainer) entityContainer).getEntity();
                    List<Step> query = tree.query(new Envelope(new Coordinate(node.getLongitude(), node.getLatitude())));
                    query.forEach(generator -> {
                        generator.getSerializer().process(entityContainer);
                        generator.getCounter().incrementAndGet();
                    });
                }
            }
        });
        reader.run();
        log.info("{}: {}", depth, tree.itemsTree());
        List<Geometry> results = Lists.newArrayList();
        for (Step object : (List<Step>) tree.itemsTree()) {
            object.getSerializer().complete();
            object.getSerializer().release();
        }
        for (Step object : (List<Step>) tree.itemsTree()) {
            if (object.getCounter().get() > 50000) {
                results.addAll(subdivide(object.getGeom(), depth + 1, object.getFilename().getAbsolutePath()));
            }
            else {
                results.add(object.getGeom());
            }
        }
        return results;
    }

    private static Geometry box(Point centroid, Coordinate corner) {
        LineString lineString = gf.createLineString(new Coordinate[] { centroid.getCoordinate(), corner });
        return lineString.getEnvelope();
    }
}
