package com.mappy.data.batches.merge;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Comparator;

import lombok.*;

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
