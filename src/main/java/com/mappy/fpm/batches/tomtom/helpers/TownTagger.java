package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.ShapefileIterator;
import com.vividsolutions.jts.geom.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Slf4j
public class TownTagger {

    private final Map<Long, Centroid> centroidsCity = newHashMap();
    private final Map<Long, Centroid> centroidsHamlet = newHashMap();

    @Inject
    public TownTagger(TomtomFolder folder) {
        File file = new File(folder.getFile("sm.shp"));
        if (file.exists()) {
            log.info("Opening {}", file.getAbsolutePath());
            try (ShapefileIterator iterator = new ShapefileIterator(file, true)) {
                while (iterator.hasNext()) {
                    Feature feature = iterator.next();

                    Centroid centroid = new Centroid()
                            .withName(feature.getString("NAME"))
                            .withPostcode(feature.getString("POSTCODE"))
                            .withAdminclass(feature.getInteger("ADMINCLASS"))
                            .withCitytyp(feature.getInteger("CITYTYP"))
                            .withDispclass(feature.getInteger("DISPCLASS"))
                            .withPoint(feature.getPoint());

                    Long id = feature.getLong("ID");
                    centroidsCity.put(id, centroid.withId(id));

                    ofNullable(feature.getLong("BUAID")).ifPresent(aLong -> centroidsHamlet.put(aLong, centroid.withId(aLong)));
                }
            }
        }
        else {
            log.info("File not found: {}", file.getAbsolutePath());
        }
    }

    public Centroid get(Long centroidId) {
        return centroidsCity.get(centroidId);
    }

    public Centroid getHamlet(Long centroidId) {
        return centroidsHamlet.get(centroidId);
    }

    public List<Centroid> getCapital(int tomtomLevel) {
        return centroidsCity.values().stream().filter(centroid -> centroid.getAdminclass() <= tomtomLevel).collect(toList());
    }

    @Data
    @Wither
    @AllArgsConstructor
    public static class Centroid {

        private final Long id;
        private final String name;
        private final String postcode;
        private final Integer adminclass;
        private final Integer citytyp;
        private final Integer dispclass;
        private final Point point;

        public Centroid() {
            this(null, null, null, null, null, null, null);
        }

        public Optional<String> getPlace() {
            Optional<String> place = empty();

            switch (citytyp) {
                case 0:
                    place = of("village");
                    break;
                case 1:
                    place = of(dispclass < 8 ? "city" : "town");
                    break;
                case 32:
                    place = of("hamlet");
                    break;
                case 64:
                    place = of("neighbourhood");
                    break;
            }
            return place;
        }
    }
}
