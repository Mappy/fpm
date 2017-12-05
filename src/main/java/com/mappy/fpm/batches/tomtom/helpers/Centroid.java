package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.utils.Feature;
import com.vividsolutions.jts.geom.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Wither;

import java.util.Optional;

import static java.util.Optional.of;

@Data
@Wither
@AllArgsConstructor
public class Centroid {

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

    public static Centroid from(Feature feature) {
        return new Centroid()
                .withName(feature.getString("NAME"))
                .withPostcode(feature.getString("POSTCODE"))
                .withAdminclass(feature.getInteger("ADMINCLASS"))
                .withCitytyp(feature.getInteger("CITYTYP"))
                .withDispclass(feature.getInteger("DISPCLASS"))
                .withPoint(feature.getPoint());
    }

    public Optional<String> getPlace() {
        Optional<String> place = of("town");

        switch (citytyp) {
            case 0:
                place = of("village");
                break;
            case 1:
            case 2:
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
