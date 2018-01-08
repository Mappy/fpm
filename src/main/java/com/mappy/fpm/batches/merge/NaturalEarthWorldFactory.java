package com.mappy.fpm.batches.merge;

import com.mappy.fpm.batches.naturalearth.discarded.CoastLinesShapefile;
import com.mappy.fpm.batches.utils.ShapefileIterator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Paths;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.merge.PolygonsUtils.*;
import static com.vividsolutions.jts.operation.union.CascadedPolygonUnion.union;

@Slf4j
public class NaturalEarthWorldFactory {
    private final String input;
    private final MultiPolygon cuttingPolygon;

    @Inject
    public NaturalEarthWorldFactory(@Named("com.mappy.fpm.naturalearth.data") String input, MultiPolygon cuttingPolygon) {
        this.input = input;
        this.cuttingPolygon = cuttingPolygon;
    }

    public NaturalEarthWorld loadNaturalEarth(TomtomWorld tomtom) {
        NaturalEarthWorld naturalEarth = new NaturalEarthWorld();
        List<Geometry> errors = newArrayList();
        try (ShapefileIterator iterator = new ShapefileIterator(Paths.get(input, "ne_10m_admin_0_countries.shp"))) {
            iterator.forEachRemaining(next -> {
                String name = next.getString("NAME");
                log.info("Processing: {}", name);
                List<Geometry> polygons = newArrayList();
                for (Polygon polygon : polygons(next.getGeometry())) {
                    Geometry intersection = cuttingPolygon.intersection(polygon);
                    if (intersection.isEmpty()) {
                        log.debug("Polygon outside the cutting polygon. Inserting.");
                        polygons.add(difference(polygon, tomtom.queryBorders(polygon)));
                    }
                    else if (!intersection.equals(polygon)) {
                        log.debug("Polygon on the border of the cutting polygon. Keeping it for later.");
                        errors.add(difference(polygon, tomtom.queryBorders(polygon)).difference(cuttingPolygon));
                    }
                    else {
                        log.debug("Polygon inside the cutting polygon. Discarding.");
                    }
                }
                if (!polygons.isEmpty()) {
                    naturalEarth.insert(new Country(union(polygons).intersection(CoastLinesShapefile.world()), name));
                }
            });
        }

        log.info("Try to add little holes in natural earth countries..");
        polygonsFrom(errors).forEach(p -> {
            Geometry buffered = p.buffer(0.001);
            naturalEarth.query(p).stream()
                    .max(Country.byArea(buffered))
                    .ifPresent(country -> {
                        log.debug("Expanding country..");
                        country.expand(p);
                    });
        });
        return naturalEarth;
    }

}