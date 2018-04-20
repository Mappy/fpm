package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.AreaType.isTheMinimumAreaType;
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.SideOfLine.*;
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationElementType.withArea;
import static java.util.Collections.emptyList;
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
        return getTransportationAreas(tomtomId, getTransportationAreaPredicate(true, LEFT.value));
    }

    public Optional<String> getBuiltUpRight(Long tomtomId) {
        return getTransportationAreas(tomtomId, getTransportationAreaPredicate(true, RIGHT.value));
    }

    public Optional<String> getSmallestAreasLeft(Long tomtomId) {
        return getMaxAreaType(tomtomId, LEFT.value)
                .flatMap(max -> getTransportationAreas(tomtomId, tr -> max.equals(tr.getAreaType()) && isSideOfLine(LEFT.value, tr)));
    }


    public Optional<String> geSmallestAreasRight(Long tomtomId) {
        return getMaxAreaType(tomtomId, RIGHT.value)
                .flatMap(max -> getTransportationAreas(tomtomId, tr -> max.equals(tr.getAreaType()) && isSideOfLine(RIGHT.value, tr)));
    }

    private Optional<String> getTransportationAreas(Long tomtomId, Predicate<TransportationArea> transportationAreaPredicate) {
        return transportationAreas.getOrDefault(tomtomId, emptyList())
                .stream()
                .filter(transportationAreaPredicate)
                .map(t -> t.getAreaId().toString())
                .findFirst();
    }

    private Optional<Integer> getMaxAreaType(Long tomtomId, Integer sideOfLine) {
        return ofNullable(transportationAreas.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(getTransportationAreaPredicate(false, sideOfLine))
                .map(TransportationArea::getAreaType)
                .max(naturalOrder());
    }

    private Predicate<TransportationArea> getTransportationAreaPredicate(Boolean needBuiltUp, Integer sideOfLine) {
        return transportationArea -> withArea(transportationArea.getType()) && isTheMinimumAreaType(transportationArea.getAreaType(), needBuiltUp) && isSideOfLine(sideOfLine, transportationArea);
    }

    private boolean isSideOfLine(Integer sideOfLine, TransportationArea transportationArea) {
        return transportationArea.getSideOfLine().equals(sideOfLine) || transportationArea.getSideOfLine().equals(BOTH_SIDES.value);
    }

    private void getTransportationAreas(DbfRow row) {
        TransportationArea geocode = TransportationArea.fromDbf(row);
        List<TransportationArea> transportationAreaList = transportationAreas.containsKey(geocode.getId()) ? transportationAreas.get(geocode.getId()) : newArrayList();
        transportationAreaList.add(geocode);
        transportationAreas.put(geocode.getId(), transportationAreaList);
    }
}
