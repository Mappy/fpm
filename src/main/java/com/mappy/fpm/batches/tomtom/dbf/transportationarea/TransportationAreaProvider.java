package com.mappy.fpm.batches.tomtom.dbf.transportationarea;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Singleton;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.asMap;
import static com.google.common.collect.Maps.newHashMap;
import static com.mappy.fpm.batches.tomtom.dbf.transportationarea.TransportationArea.TransportationElementType.isARoadElement;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Singleton
public class TransportationAreaProvider {
    private final Map<Long, List<TransportationArea>> transportationAreas = newHashMap();
    private final TomtomFolder folder;


    public TransportationAreaProvider(TomtomFolder folder) {
        this.folder = folder;
    }

    public void loadTransportationAreaAttributes(String filename) {
        transportationAreas.putAll(readFile(filename));
    }

    public String getAreas(Long tomtomId) {
        return ofNullable(transportationAreas.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(getTransportationAreaPredicate())
                .sorted(Comparator.comparing(TransportationArea::getSideOfLine))
                .map(t -> t.getAreaId().toString())
                .collect(Collectors.joining(";"));
    }

    private Predicate<TransportationArea> getTransportationAreaPredicate() {
        return transportationArea -> isARoadElement(transportationArea.getType()) && TransportationArea.AreaType.isTheMinimumAreaType(transportationArea.getAreaType());
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
