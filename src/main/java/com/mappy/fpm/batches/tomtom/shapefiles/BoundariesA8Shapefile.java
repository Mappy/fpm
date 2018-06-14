package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

import static java.util.Optional.empty;

public class BoundariesA8Shapefile extends BoundariesShapefile {

    @Inject
    public BoundariesA8Shapefile(TomtomFolder folder, TownTagger townTagger, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator) {
        super(folder.getFile("a8.shp"), 8, null, townTagger, nameProvider, osmLevelGenerator);
        if(new File(folder.getFile("a8.shp")).exists()) {
            this.nameProvider.loadAlternateCityNames();
        }
    }

    @Override
    public String getOutputFileName() {
        return "a8";
    }

    @Override
    protected Optional<RelationMember> getAdminCenter(GeometrySerializer serializer, Feature feature) {
        return getTown(serializer, feature);
    }
}