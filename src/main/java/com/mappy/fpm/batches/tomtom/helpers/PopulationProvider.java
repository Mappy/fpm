package com.mappy.fpm.batches.tomtom.helpers;

import com.mappy.fpm.batches.utils.Feature;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;

@Singleton
public class PopulationProvider {

    private final Map<Long, String> populationMap = new HashMap<>();

    public void putPopulation(Feature feature) {
        populationMap.put(feature.getLong("CITYCENTER"), valueOf(feature.getLong("POP")));
    }

    public Optional<String> getPop(Long id) {
        return ofNullable(populationMap.get(id));
    }
}