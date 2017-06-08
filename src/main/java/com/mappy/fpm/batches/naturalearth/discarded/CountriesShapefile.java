package com.mappy.fpm.batches.naturalearth.discarded;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mappy.fpm.batches.geonames.AlternateName;
import com.mappy.fpm.batches.geonames.Geonames;
import com.mappy.fpm.batches.naturalearth.NaturalEarthShapefile;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Slf4j
public class CountriesShapefile extends NaturalEarthShapefile {
    private final Geonames geonames;

    @Inject
    public CountriesShapefile(@Named("com.mappy.fpm.naturalearth.data") String input, Geonames geonames) {
        super(input + "/ne_10m_admin_0_countries.shp");
        this.geonames = geonames;
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serializer.write(feature.getMultiPolygon().getInteriorPoint(), names(feature).put("place", "country").build());
    }

    private Builder<String, String> names(Feature feature) {
        String name = feature.getString("name");
        String isocode = feature.getString("iso_a3");
        Builder<String, String> base = ImmutableMap.<String, String> builder().put("name", name).put("ISO3166-1:alpha3", isocode);
        List<AlternateName> frenchNames = geonames.frenchNames(isocode);
        if (frenchNames.isEmpty()) {
            log.info("No french name for {}", name);
        } else {
            base = base.put("name:fr", frenchNames.get(0).getValue());
        }
        return base;
    }
}