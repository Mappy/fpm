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
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationArea.AreaType.isTheMinimumAreaType;
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationArea.TransportationElementType.isARoadElement;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Slf4j
@Singleton
public class TransportationAreaProvider extends TomtomDbfReader {
    private final Map<Long, List<TransportationArea>> transportationAreas = newHashMap();

    @Inject
    public TransportationAreaProvider(TomtomFolder folder) {
        super(folder);
        readFile("ta.dbf", this::getTransportationAreas);
    }

    public Optional<String> getBuiltUp(Long tomtomId) {
        return getTransportationAreas(tomtomId, getTransportationAreaPredicate(true));
    }

    public Optional<String> getSmallestAreas(Long tomtomId) {
        Optional<TransportationArea> min = getMaxAreaType(tomtomId);

        if (min.isPresent()) {
            return getTransportationAreas(tomtomId, transportationArea -> transportationArea.getAreaType().equals(min.get().getAreaType()));
        }
        return empty();
    }

    private Optional<String> getTransportationAreas(Long tomtomId, Predicate<TransportationArea> transportationAreaPredicate) {
        return ofNullable(ofNullable(transportationAreas.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(transportationAreaPredicate)
                .sorted(comparing(TransportationArea::getSideOfLine))
                .map(t -> t.getAreaId().toString())
                .collect(joining(";")));
    }

    private Optional<TransportationArea> getMaxAreaType(Long tomtomId) {
        return ofNullable(transportationAreas.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(getTransportationAreaPredicate(false))
                .max(comparing(TransportationArea::getAreaType));
    }

    private Predicate<TransportationArea> getTransportationAreaPredicate(Boolean needBuiltUp) {
        return transportationArea -> isARoadElement(transportationArea.getType()) && isTheMinimumAreaType(transportationArea.getAreaType(), needBuiltUp);
    }

    private void getTransportationAreas(DbfRow row) {
        TransportationArea geocode = TransportationArea.fromDbf(row);
        List<TransportationArea> transportationAreaList = transportationAreas.containsKey(geocode.getId()) ? transportationAreas.get(geocode.getId()) : newArrayList();
        transportationAreaList.add(geocode);
        transportationAreas.put(geocode.getId(), transportationAreaList);
    }

}
