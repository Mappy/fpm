package com.mappy.data.batches.tomtom;

import lombok.Value;
import net.morbz.osmonaut.EntityFilter;
import net.morbz.osmonaut.IOsmonautReceiver;
import net.morbz.osmonaut.Osmonaut;
import net.morbz.osmonaut.osm.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;
import static net.morbz.osmonaut.osm.EntityType.*;

public class Tomtom2OsmTestUtils {

    @Value
    public static class PbfContent {
        List<Way> ways;
        List<Relation> relations;
        List<Node> nodes;
    }

    public static PbfContent read(File pbfFile) {
        AtomicBoolean node = new AtomicBoolean(false);
        AtomicBoolean way = new AtomicBoolean(false);
        AtomicBoolean relation = new AtomicBoolean(false);
        Osmonaut naut = new Osmonaut(pbfFile.getAbsolutePath(), new EntityFilter(true, true, true));
        PbfContent pbfContent = new PbfContent(newArrayList(), newArrayList(), newArrayList());
        naut.scan(new IOsmonautReceiver() {
            @Override
            public boolean needsEntity(EntityType type, Tags tags) {
                return true;
            }

            @Override
            public void foundEntity(Entity entity) {
                if (NODE == entity.getEntityType()) {
                    if (way.get() && relation.get()) {
                        throw new IllegalStateException("Way or relation before node");
                    }
                    pbfContent.getNodes().add((Node) entity);
                    node.set(true);
                }
                else if (WAY == entity.getEntityType()) {
                    if (relation.get()) {
                        throw new IllegalStateException("Relation before node");
                    }
                    pbfContent.getWays().add((Way) entity);
                    way.set(true);
                }
                else if (RELATION == entity.getEntityType()) {
                    pbfContent.getRelations().add((Relation) entity);
                    relation.set(true);
                }
            }
        });
        return pbfContent;
    }
}
