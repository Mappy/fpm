package com.mappy.fpm.batches.merge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class NaturalEarthWorld {

    private final List<Country> countries = newArrayList();
    private final STRtree index = new STRtree();

    public void insert(Country feature) {
        countries.add(feature);
        index.insert(feature.getGeometry().getEnvelopeInternal(), feature);
    }

    @SuppressWarnings("unchecked")
    public List<Country> query(Geometry geom) {
        return (List<Country>) index.query(geom.getEnvelopeInternal());
    }

    public List<Country> getCountries() {
        log.info("Retrieve countries without artifacts due to union");
        return countries.stream().map(g -> new Country(g.getGeometry().buffer(0.0000001).buffer(-0.0000001), g.getName())).collect(toList());
    }

    public static Polygon world() {
        try {
            return (Polygon) new WKTReader().read("POLYGON((-179.9999 89.9999,179.9999 89.9999,179.9999 -89.9999,-179.9999 -89.9999,-179.9999 89.9999))");
        }
        catch (ParseException e) {
            throw propagate(e);
        }
    }
}