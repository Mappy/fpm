package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;

@Slf4j
@Singleton
public class TransportationAreaProvider {
    private final Map<Long, List<TransportationArea>> transportationAreas = newHashMap();
    private final TomtomFolder folder;

    @Inject
    public TransportationAreaProvider(TomtomFolder folder) {
        this.folder = folder;
    }

    public void loadTransportationAreaAttributes(String filename) {
        transportationAreas.putAll(readFile(filename));
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


    private Map<Long, List<TransportationArea>> readFile(String filename) {
        Map<Long, List<TransportationArea>> transportations = newHashMap();

        File file = new File(folder.getFile(filename));
        if (file.exists()) {
            log.info("Reading {}", file);
            try (DbfReader reader = new DbfReader(file)) {
                DbfRow row;
                Stopwatch stopwatch = Stopwatch.createStarted();
                int counter = 0;

                while ((row = reader.nextRow()) != null) {
                    TransportationArea geocode = TransportationArea.fromDbf(row);
                    List<TransportationArea> transportationAreaList = transportations.containsKey(geocode.getId()) ? transportations.get(geocode.getId()) : newArrayList();
                    transportationAreaList.add(geocode);
                    transportations.put(geocode.getId(), transportationAreaList);
                    counter++;
                }
                long time = stopwatch.elapsed(MILLISECONDS);
                stopwatch.stop();
                log.info("Added {} object(s){}", counter, counter > 0 ? " in " + time + " ms at rate " + String.format("%.2f", counter * 1.0 / time) + " obj/ms" : "");
            }

        } else {
            log.info("File not found : {}", file.getAbsolutePath());
        }

        return transportations;
    }

}
