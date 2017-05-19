package com.mappy.data.batches.api;

import com.github.davidmoten.geo.LatLong;
import com.mappy.data.batches.utils.Geohash;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Throwables.propagate;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SplittedAreas {
    private static final GeometryFactory gf = new GeometryFactory();
    private final STRtree tree;

    public SplittedAreas() {
        try {
            this.tree = new STRtree();
            int i = 1;
            WKTReader reader = new WKTReader();
            for (String line : IOUtils.readLines(getClass().getResourceAsStream("/split.csv"), UTF_8)) {
                tree.insert(reader.read(line).getEnvelopeInternal(), String.valueOf(i++));
            }
        }
        catch (IOException | ParseException e) {
            throw propagate(e);
        }
    }

    public List<String> file(BoundingBox bbox) {
        return file(bbox.envelope());
    }

    @SuppressWarnings("unchecked")
    public List<String> file(Envelope envelope) {
        return tree.query(envelope);
    }

    @SuppressWarnings("unchecked")
    public String file(double x, double y) {
        List<String> query = tree.query(point(x, y));
        return !query.isEmpty() ? query.get(0) : null;
    }

    public String file(long geohash) {
        LatLong decodeGeohash = Geohash.decodeGeohash(geohash);
        return file(decodeGeohash.getLon(), decodeGeohash.getLat());
    }

    private static Envelope point(double x, double y) {
        return gf.createPoint(new Coordinate(x, y)).getEnvelopeInternal();
    }
}
