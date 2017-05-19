package com.mappy.data.batches.naturalearth.discarded;

import com.google.common.collect.ImmutableMap;
import com.mappy.data.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.data.batches.utils.Feature;
import com.mappy.data.batches.utils.GeometrySerializer;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static com.google.common.base.Throwables.propagate;

public class CoastLinesShapefile extends NaturalEarthShapefile {
    private static final GeometryFactory gf = new GeometryFactory();
    private static final Map<String, String> tags = ImmutableMap.of("natural", "coastline");
    private static final Polygon bbox = world();

    @Inject
    public CoastLinesShapefile(@Named("com.mappy.data.naturalearth.data") String input) {
        super(input + "/ne_10m_land.shp");
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        MultiPolygon multiPolygon = feature.getMultiPolygon();
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
            write(serializer, (Polygon) cropIfNeeded(polygon).reverse());
        }
    }

    private static void write(GeometrySerializer serializer, Polygon p) {
        serializer.write(gf.createMultiLineString(new LineString[] { p.getExteriorRing() }), tags);
        if (p.getNumInteriorRing() != 0) {
            for (int j = 0; j < p.getNumInteriorRing(); j++) {
                serializer.write(gf.createMultiLineString(new LineString[] { p.getInteriorRingN(j) }), tags);
            }
        }
    }

    private static Polygon cropIfNeeded(Polygon polygon) {
        if (bbox.contains(polygon)) {
            return polygon;
        }
        return (Polygon) bbox.intersection(polygon);
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
