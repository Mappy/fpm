package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.BoundariesShapefile;
import com.mappy.fpm.batches.tomtom.helpers.OsmLevelGenerator;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;

public class BoundariesA9Shapefile extends BoundariesShapefile {

    private final TownTagger townTagger;

    @Inject
    public BoundariesA9Shapefile(TomtomFolder folder, NameProvider nameProvider, OsmLevelGenerator osmLevelGenerator, TownTagger townTagger) {
        super(folder.getFile("a9.shp"), 9, nameProvider, osmLevelGenerator);
        this.townTagger = townTagger;
        if(new File(folder.getFile("a9.shp")).exists()) {
            nameProvider.loadFromCityFile("smnm.dbf");
        }
    }

    @Override
    public String getOutputFileName() {
        return "a9";
    }

    @Override
    public void finishRelation(GeometrySerializer serializer, Map<String, String> adminTags, List<RelationMember> members, Feature feature) {

        TownTagger.Centroid cityCenter = townTagger.get(feature.getLong("CITYCENTER"));

        if (cityCenter != null) {
            Map<String, String> tags = newHashMap();

            tags.put("name", cityCenter.getName());
            ofNullable(cityCenter.getPostcode()).ifPresent(code -> tags.put("addr:postcode", code));
            cityCenter.getPlace().ifPresent(p -> tags.put("place", p));
            tags.putAll(nameProvider.getAlternateCityNames(cityCenter.getId()));

            switch (cityCenter.getAdminclass()) {
                case 0:
                    tags.put("capital", "yes");
                    break;
                case 1:
                    tags.put("capital", "1");
                    break;
                case 7:
                    tags.put("capital", "6");
                    break;
                case 8:
                    tags.put("capital", "8");
                    break;
                case 9:
                    tags.put("capital", "9");
                    break;
            }


            Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), tags);
            node.ifPresent(adminCenter -> members.add(new RelationMember(adminCenter.getId(), Node, "admin_center")));
        }

        serializer.writeRelation(members, adminTags);
    }
}
