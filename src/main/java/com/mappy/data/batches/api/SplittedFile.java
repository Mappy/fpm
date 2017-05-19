package com.mappy.data.batches.api;

import java.util.Iterator;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

public interface SplittedFile {
    Iterator<Relation> getRelations();

    Iterator<Node> nodesWithin(BoundingBox bbox);

    Iterator<Way> getWays();

    Node getNodeById(Long id);
}
