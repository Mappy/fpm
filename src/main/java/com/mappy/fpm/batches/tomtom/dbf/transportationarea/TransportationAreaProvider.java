package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.AreaType.isTheMinimumAreaType;
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationElementType.withArea;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Optional.ofNullable;

@Slf4j
@Singleton
public class TransportationAreaProvider extends TomtomDbfReader {
    private final Map<Long, List<TransportationArea>> transportationAreas = newHashMap();

    @Inject
    public TransportationAreaProvider(TomtomFolder folder) {
        super(folder);
        readFile("ta.dbf", this::getTransportationAreas);
    }

    public Optional<String> getBuiltUpLeft(Long tomtomId) {
        return getTransportationAreas(tomtomId, getTransportationAreaPredicate(true), comparing(TransportationArea::getSideOfLine).reversed());
    }

    public Optional<String> getBuiltUpRight(Long tomtomId) {
        return getTransportationAreas(tomtomId, getTransportationAreaPredicate(true), comparing(TransportationArea::getSideOfLine));
    }

    public Optional<String> getLeftSmallestAreas(Long tomtomId) {
        return getMaxAreaType(tomtomId)
                .flatMap(max -> getTransportationAreas(tomtomId, tr -> max.equals(tr.getAreaType()), comparing(TransportationArea::getSideOfLine)));
    }


    public Optional<String> getRightSmallestAreas(Long tomtomId) {
        return getMaxAreaType(tomtomId)
                .flatMap(max -> getTransportationAreas(tomtomId, tr -> max.equals(tr.getAreaType()), comparing(TransportationArea::getSideOfLine).reversed()));
    }

    private Optional<String> getTransportationAreas(Long tomtomId, Predicate<TransportationArea> transportationAreaPredicate, Comparator<TransportationArea> sorted) {
        return transportationAreas.getOrDefault(tomtomId, emptyList())
                .stream()
                .filter(transportationAreaPredicate)
                .sorted(sorted)
                .map(t -> t.getAreaId().toString())
                .findFirst();
    }

    private Optional<Integer> getMaxAreaType(Long tomtomId) {
        return ofNullable(transportationAreas.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(getTransportationAreaPredicate(false))
                .map(TransportationArea::getAreaType)
                .max(naturalOrder());
    }

    private Predicate<TransportationArea> getTransportationAreaPredicate(Boolean needBuiltUp) {
        return transportationArea -> withArea(transportationArea.getType()) && isTheMinimumAreaType(transportationArea.getAreaType(), needBuiltUp);
    }

    private void getTransportationAreas(DbfRow row) {
        TransportationArea geocode = TransportationArea.fromDbf(row);
        List<TransportationArea> transportationAreaList = transportationAreas.containsKey(geocode.getId()) ? transportationAreas.get(geocode.getId()) : newArrayList();
        transportationAreaList.add(geocode);
        transportationAreas.put(geocode.getId(), transportationAreaList);
    }
}
