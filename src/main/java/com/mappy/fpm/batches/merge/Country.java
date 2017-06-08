package com.mappy.fpm.batches.merge;

import com.vividsolutions.jts.geom.Geometry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Comparator;

@Data
@AllArgsConstructor
public class Country {
    private Geometry geometry;
    private final String name;

    public static Comparator<Country> byArea(Geometry given) {
        return Comparator.comparing(country -> country.getGeometry().intersection(given).getArea());
    }

    public void expand(Geometry geom) {
        geometry = geometry.union(geom);
    }
}
