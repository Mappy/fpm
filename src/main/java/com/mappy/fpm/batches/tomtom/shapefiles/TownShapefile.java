package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.CityType;
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
            nameProvider.loadAlternateCityNames("smnm.shp");
        }
    }

    @Override
    public String getOutputFileName() {
        return "sm";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {

        if (isAnOtherAdministrativeArea(feature)) {
            HashMap<String, String> tags = newHashMap();

            tags.put("name", feature.getString("NAME"));
            tags.putAll(nameProvider.getAlternateCityNames(feature.getLong("ID")));
            tags.put("place", CityType.getOsmValue(feature.getInteger("CITYTYP"), feature.getInteger("DISPCLASS")));

            serializer.writePoint(feature.getPoint(), tags);
        }
    }

    private boolean isAnOtherAdministrativeArea(Feature feature) {
        return feature.getInteger("ADMINCLASS") > 9;
    }
}
