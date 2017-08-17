package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.PopulationProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class TownShapefile extends TomtomShapefile {

    private final NameProvider nameProvider;
    private PopulationProvider populationProvider;


    @Inject
    public TownShapefile(NameProvider nameProvider, TomtomFolder folder, PopulationProvider populationProvider) {
        super(folder.getFile("sm.shp"));
        this.nameProvider = nameProvider;
        this.nameProvider.loadFromFile("smnm.dbf", "NAME", false);
        this.populationProvider = populationProvider;
    }

    public void serialize(GeometrySerializer geometrySerializer, Feature feature) {
        Map<String, String> tags = newHashMap();
        String name = feature.getString("NAME");
        if (name != null) {
            tags.put("name", name);
        }
        switch (feature.getInteger("ADMINCLASS")) {
            case 1:
                tags.put("place", "city");
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                tags.put("place", "town");
                break;
            case 8:
            case 9:
                tags.put("place", "village");
                break;
        }
        tags.putAll(nameProvider.getAlternateNames(feature.getLong("ID")));
        populationProvider.getPop(feature.getLong("ID")).ifPresent((pop) -> tags.put("population", pop));
        geometrySerializer.write(feature.getPoint(), tags);
    }
}
