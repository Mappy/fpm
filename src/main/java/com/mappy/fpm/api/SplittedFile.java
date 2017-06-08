package com.mappy.fpm.api;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Iterator;

public interface SplittedFile {
    Iterator<Relation> getRelations();

    Iterator<Node> nodesWithin(BoundingBox bbox);

    Iterator<Way> getWays();

    Node getNodeById(Long id);
}
