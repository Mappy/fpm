package com.mappy.fpm.batches.tomtom.helpers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mappy.fpm.batches.tomtom.TomtomShapefile;
import com.mappy.fpm.batches.tomtom.dbf.names.NameProvider;
import com.mappy.fpm.batches.utils.Feature;
import com.mappy.fpm.batches.utils.GeometrySerializer;
import com.mappy.fpm.batches.utils.LongLineSplitter;
import com.neovisionaries.i18n.CountryCode;
import com.vividsolutions.jts.algorithm.Centroid;
import com.vividsolutions.jts.geom.*;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Node;
import static org.openstreetmap.osmosis.core.domain.v0_6.EntityType.Way;

public class BoundariesShapefile extends TomtomShapefile {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private final String adminLevel;
    private final String tomtomLevel;
    private final NameProvider nameProvider;


    protected BoundariesShapefile(String filename, int adminLevel, int tomtomLevel, NameProvider nameProvider) {
        super(filename);
        this.adminLevel = String.valueOf(adminLevel);
        this.tomtomLevel = String.valueOf(tomtomLevel);
        this.nameProvider = nameProvider;
        this.nameProvider.loadFromFile("an.dbf", "NAME", false);
    }

    @Override
    public void serialize(GeometrySerializer serializer, Feature feature) {
        serialize(serializer, feature, Lists.newArrayList());
    }

    public void serialize(GeometrySerializer serializer, Feature feature, List<RelationMember> members) {
        String name = feature.getString("NAME");
        Long extId = feature.getLong("ID");
        String order0 = feature.getString("ORDER0" + tomtomLevel);
        Optional<String> population = ofNullable(valueOf(feature.getLong("POP")));
        String insee = CountryCode.getByCode(order0) == null ? order0 : String.valueOf(CountryCode.getByCode(order0).getNumeric());
        Map<String, String> tags = nameProvider.getAlternateNames(extId);
        ImmutableMap<String, String> wayTags = of(
                "boundary", "administrative",
                "admin_level", adminLevel);
        tags.putAll(wayTags);

        ImmutableMap<String, String> relationTags = of(
                "type", "boundary",
                "ref:tomtom", extId.toString(),
                "ref:INSEE", insee
        );
        tags.putAll(relationTags);
        population.ifPresent(pop ->
            tags.put("population", pop)
        );
        writeRelations(serializer, feature, members, name, tags, wayTags);
    }

    public void writePoint(GeometrySerializer serializer, List<RelationMember> members, String name, MultiPolygon multiPolygon) {
        Coordinate centPt = Centroid.getCentroid(multiPolygon);
        Optional<Node> node = serializer.writePoint(GEOMETRY_FACTORY.createPoint(centPt), of("name", name));
        node.ifPresent(node1 ->
                members.add(new RelationMember(node1.getId(), Node, "admin_center"))
        );
    }

    private void writeRelations(GeometrySerializer serializer, Feature feature, List<RelationMember> members, String name, Map<String, String> tags, ImmutableMap<String, String> wayTags) {
        if (name != null) {
            tags.put("name", name);
            MultiPolygon multiPolygon = feature.getMultiPolygon();
            writePoint(serializer, members, name, multiPolygon);
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                for (Geometry geom : LongLineSplitter.split(polygon.getExteriorRing(), 100)) {

                    Way way = serializer.write((LineString) geom, wayTags);
                    members.add(new RelationMember(way.getId(), Way, "outer"));
                }
            }

            serializer.writeRelation(members, tags);
        }
    }

}
