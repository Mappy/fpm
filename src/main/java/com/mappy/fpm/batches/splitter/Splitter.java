package com.mappy.fpm.batches.splitter;

import com.github.davidmoten.geo.LatLong;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mappy.fpm.batches.utils.Geohash;
import com.vividsolutions.jts.geom.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class Splitter {
    private final String filename;
    private final SplitterSerializers kml;

    @Inject
    public Splitter(@Named("com.mappy.fpm.serializer.output") String filename, SplitterSerializers kml) {
        this.filename = filename;
        this.kml = kml;
    }

    public static void main(String[] args) {
        new Splitter(args[0], new SplitterSerializers(args[1], args[2])).run();
    }

    public void run() {
        File file = new File(filename);
        Stopwatch stopwatch = Stopwatch.createStarted();

        Multimap<Long, Long> wayByRelations = ArrayListMultimap.create();
        Multimap<Long, Integer> borderNodeTargets = ArrayListMultimap.create();
        firstPass(file, wayByRelations, borderNodeTargets);
        finalPass(wayByRelations, borderNodeTargets, file);

        kml.close();
        log.info("time: {}s", stopwatch.elapsed(SECONDS));
    }

    private void finalPass(Multimap<Long, Long> wayByRelations, Multimap<Long, Integer> borderNodeTargets, File file) {
        Multimap<Long, Integer> relationTargets = ArrayListMultimap.create();
        read(file, new SplitterSink("final pass") {
            @Override
            public void process(NodeContainer node) {
                long id = node.getEntity().getId();
                if (borderNodeTargets.containsKey(id)) {
                    for (int target : borderNodeTargets.get(id)) {
                        kml.serializer(target).process(node);
                    }
                }
                else {
                    kml.serializer(node.getEntity().getLongitude(), node.getEntity().getLatitude()).process(node);
                }
            }

            @Override
            public void process(WayContainer way) {
                List<Integer> targets = kml.serializer(enveloppe(way));
                if (wayByRelations.containsKey(way.getEntity().getId())) {
                    for (Long rel : wayByRelations.get(way.getEntity().getId())) {
                        relationTargets.putAll(rel, targets);
                    }
                }
                for (int target : targets) {
                    kml.serializer(target).process(way);
                }
            }

            @Override
            public void process(RelationContainer rel) {
                List<Integer> serializer = kml.serializer(enveloppe(rel));
                Set<Integer> targets = Sets.newHashSet(serializer);
                targets.addAll(Sets.newHashSet(relationTargets.get(rel.getEntity().getId())));
                for (int target : targets) {
                    kml.serializer(target).process(rel);
                }
            }
        });
    }

    private void firstPass(File file, Multimap<Long, Long> wayByRelations, Multimap<Long, Integer> borderNodeTargets) {
        read(file, new SplitterSink("first pass") {
            @Override
            public void process(NodeContainer node) {}

            @Override
            public void process(WayContainer way) {
                List<Integer> targets = kml.serializer(enveloppe(way));
                if (targets.size() > 1) {
                    for (WayNode wn : way.getEntity().getWayNodes()) {
                        for (int target : targets) {
                            borderNodeTargets.put(wn.getNodeId(), target);
                        }
                    }
                }
            }

            @Override
            public void process(RelationContainer rel) {
                for (RelationMember wn : rel.getEntity().getMembers()) {
                    wayByRelations.put(wn.getMemberId(), rel.getEntity().getId());
                }
            }
        });
    }

    private static Envelope enveloppe(WayContainer way) {
        Envelope env = new Envelope();
        for (WayNode wn : way.getEntity().getWayNodes()) {
            LatLong geohash = Geohash.decodeGeohash(wn.getNodeId());
            env.expandToInclude(geohash.getLon(), geohash.getLat());
        }
        return env;
    }

    private static Envelope enveloppe(RelationContainer rel) {
        Envelope env = new Envelope();
        for (RelationMember wn : rel.getEntity().getMembers()) {
            LatLong geohash = Geohash.decodeGeohash(wn.getMemberId());
            env.expandToInclude(geohash.getLon(), geohash.getLat());
        }
        return env;
    }

    public static void read(File file, SplitterSink sink) {
        PbfReader reader = new PbfReader(file, 2);
        reader.setSink(sink);
        reader.run();
    }
}
