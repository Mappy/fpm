package com.mappy.fpm.batches.tomtom.shapefiles;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.tomtom.helpers.Centroid;
import com.mappy.fpm.batches.tomtom.helpers.PolygonBoundaryBuilder;
import com.mappy.fpm.batches.tomtom.helpers.TownTagger;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.vividsolutions.jts.algorithm.Centroid.getCentroid;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;

public class BuiltUpShapefile extends TomtomShapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final NameProvider nameProvider;
    private final TownTagger townTagger;

    @Inject
    public BuiltUpShapefile(TomtomFolder folder, NameProvider nameProvider, TownTagger townTagger) {
        super(folder.getFile("bu.shp"));
        this.nameProvider = nameProvider;
        this.townTagger = townTagger;

        if (new File(folder.getFile("bu.shp")).exists()) {
            nameProvider.loadAlternateNames("smnm.dbf");
        }
    }

    @Override
    public String getOutputFileName() {
        return "bu";
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        String name = feature.getString("NAME");
        if (name != null) {
            Long extId = feature.getLong("ID");

            Map<String, String> tags = nameProvider.getAlternateNames(extId);
            tags.put("ref:tomtom", String.valueOf(extId));
            tags.put("name", name);
            Centroid cityCenter = townTagger.getBuiltUpCentroid(extId);

            List<RelationMember> members = newArrayList();
            MultiPolygon multiPolygon = feature.getMultiPolygon();

            getLabel(serializer, tags, multiPolygon).ifPresent(members::add);
            ofNullable(cityCenter).ifPresent(centroid -> getAdminCenter(serializer, name, centroid).ifPresent(members::add));

            Map<String, String> wayTags = newHashMap(of("name", name));
            ofNullable(cityCenter).ifPresent(place -> wayTags.put("place", place.getPlace()));

            PolygonBoundaryBuilder.addPolygons(serializer, members, multiPolygon, wayTags);

            tags.putAll(of("type", "multipolygon", "landuse", "residential", "layer", "10"));
            serializer.write(members, tags);
        }
    }

    private Optional<RelationMember> getLabel(GeometrySerializer serializer, Map<String, String> tags, MultiPolygon multiPolygon) {
        Optional<Node> labelNode = serializer.writePoint(GEOMETRY_FACTORY.createPoint(getCentroid(multiPolygon)), tags);
        return labelNode.map(node -> new RelationMember(node.getId(), Node, "label"));
    }

    private Optional<RelationMember> getAdminCenter(GeometrySerializer serializer, String name, Centroid cityCenter) {

        Map<String, String> adminTags = nameProvider.getAlternateNames(cityCenter.getId());
        adminTags.put("name", name);
        adminTags.put("place", cityCenter.getPlace());
        ofNullable(cityCenter.getPostcode()).ifPresent(code -> adminTags.put("addr:postcode", code));

        if (serializer.containPoint(cityCenter.getPoint())) {
            cityCenter.getPoint().getCoordinate().x += 0.00001;
            cityCenter.getPoint().getCoordinate().y += 0.00001;
        }

        Optional<Node> node = serializer.writePoint(cityCenter.getPoint(), adminTags);
        Long adminCenter = node.map(Entity::getId).orElse(0L);
        return of(new RelationMember(adminCenter, Node, "admin_centre"));

    }
}