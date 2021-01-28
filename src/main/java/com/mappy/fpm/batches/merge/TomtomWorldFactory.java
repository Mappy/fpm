package com.mappy.fpm.batches.merge;

import com.mappy.fpm.batches.utils.ShapefileIterator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.mappy.fpm.batches.merge.NaturalEarthWorld.world;
import static com.mappy.fpm.batches.merge.PolygonsUtils.polygons;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class TomtomWorldFactory {

    private final String input;
    private final MultiPolygon cuttingPolygon;

    @Inject
    public TomtomWorldFactory(@Named("com.mappy.fpm.tomtom.data") String input, MultiPolygon cuttingPolygon) {
        this.input = input;
        this.cuttingPolygon = cuttingPolygon;
    }

    public TomtomWorld loadTomtom() throws IOException {
        TomtomWorld world = new TomtomWorld();
        List<Path> a0s = Files.walk(Paths.get(input)).filter(p -> p.toString().endsWith("______________a0.shp")).collect(toList());
        a0s.stream().parallel().forEach(path -> {
            try (ShapefileIterator iterator = new ShapefileIterator(path.toFile(), true)) {
                iterator.forEachRemaining(next -> {
                    String name = next.getString("NAME");
                    if (isNotBlank(name)) {
                        log.info("Processing: {}", name);
                        Geometry highres = next.getGeometry().intersection(world());
                        for (Polygon polygon : polygons(highres)) {
                            if (cuttingPolygon.contains(polygon)) {
                                log.info("Polygon inside the cutting polygon. Inserting.");
                            }
                            else if (cuttingPolygon.intersects(polygon)) {
                                log.info("Polygon on the border of the cutting polygon. Inserting.");
                                world.insertBorderPolygon(polygon);
                            }
                            else {
                                throw new RuntimeException("Polygon outside the cutting polygon. Nothing should be outside or not crossing.");
                            }
                        }
                        world.insert(new Country(highres, name));
                    }
                });
            }
        });
        return world;
    }
}
