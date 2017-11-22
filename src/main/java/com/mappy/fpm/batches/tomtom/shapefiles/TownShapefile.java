package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;

public class TownShapefile extends TomtomShapefile {

    private final NameProvider nameProvider;

    @Inject
    public TownShapefile(TomtomFolder folder, NameProvider nameProvider) {
        super(folder.getFile("sm.shp"));
        this.nameProvider = nameProvider;
        if (new File(folder.getFile("sm.shp")).exists()) {
            nameProvider.loadFromCityFile("smnm.shp");
        }
    }

    @Override
    public String getOutputFileName() {
        return "sm";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {

        if (feature.getInteger("ADMINCLASS") > 9) {
            HashMap<String, String> tags = newHashMap();

            tags.put("name", feature.getString("NAME"));
            tags.putAll(nameProvider.getAlternateCityNames(feature.getLong("ID")));

            switch (feature.getInteger("CITYTYP")) {
                case 0:
                    tags.put("place", "hamlet");
                    break;
                case 64:
                    tags.put("place", "neighbourhood");
                    break;
            }

            serializer.writePoint(feature.getPoint(), tags);
        }
    }
}
