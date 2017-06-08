package com.mappy.fpm.batches.merge;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;
import lombok.Data;

import java.util.List;

@Data
public class TomtomWorld {
    private final List<Country> countries = Lists.newCopyOnWriteArrayList();
    private final STRtree borderIndex = new STRtree();

    public void insert(Country geom) {
        countries.add(geom);
    }

    public void insertBorderPolygon(Geometry geom) {
        borderIndex.insert(geom.getEnvelopeInternal(), geom);
    }

    @SuppressWarnings("unchecked")
    public List<Geometry> queryBorders(Geometry geom) {
        return borderIndex.query(geom.getEnvelopeInternal());
    }
}