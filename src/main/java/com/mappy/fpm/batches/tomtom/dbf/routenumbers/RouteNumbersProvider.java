package com.mappy.fpm.batches.tomtom.dbf.routenumbers;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class RouteNumbersProvider {

    private final Map<Long, List<RouteNumbers>> routenumbers = newHashMap();
    private final TomtomFolder folder;
    private final Integer INTERNATIONAL_ROUTE = 5;

    @Inject
    public RouteNumbersProvider(TomtomFolder folder) {
        this.folder = folder;
    }

    public void loadGeocodingAttributes(String filename) {
        routenumbers.putAll(readFile(filename));
    }

    public Optional<String> getInternationalRouteNumbers(Long id) {
        return getRoute(id, routeNumbers -> INTERNATIONAL_ROUTE.equals(routeNumbers.getRouteNumberType()));
    }

    public Optional<String> getNationalRouteNumbers(Long id) {
        return getRoute(id, routeNumbers -> !INTERNATIONAL_ROUTE.equals(routeNumbers.getRouteNumberType()));
    }

    private Optional<String> getRoute(Long id, Predicate<RouteNumbers> routeNumbersPredicate) {
        return ofNullable(routenumbers.get(id))
                .orElse(ImmutableList.of())
                .stream()
                .filter(routeNumbersPredicate)
                .sorted(Comparator.comparing(RouteNumbers::getRouteNumberPriority))
                .map(RouteNumbers::getFullRouteNumber)
                .findFirst();
    }

    private Map<Long, List<RouteNumbers>> readFile(String filename) {
        Map<Long, List<RouteNumbers>> routes = newHashMap();

        File file = new File(folder.getFile(filename));
        if (file.exists()) {
            log.info("Reading {}", file);
            try (DbfReader reader = new DbfReader(file)) {
                DbfRow row;
                Stopwatch stopwatch = Stopwatch.createStarted();
                int counter = 0;

                while ((row = reader.nextRow()) != null) {
                    RouteNumbers routeNumbers = RouteNumbers.fromDbf(row);
                    List<RouteNumbers> geocodingAttributes = routes.containsKey(routeNumbers.getId()) ? routes.get(routeNumbers.getId()) : newArrayList();
                    geocodingAttributes.add(routeNumbers);
                    routes.put(routeNumbers.getId(), geocodingAttributes);
                    counter++;
                }
                long time = stopwatch.elapsed(MILLISECONDS);
                stopwatch.stop();
                log.info("Added {} object(s){}", counter, counter > 0 ? " in " + time + " ms at rate " + String.format("%.2f", counter * 1.0 / time) + " obj/ms" : "");
            }

        } else {
            log.info("File not found : {}", file.getAbsolutePath());
        }

        return routes;
    }
}
