package com.mappy.fpm.batches.tomtom.dbf.geocodes;

import com.google.common.base.Enums;
import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import com.mappy.fpm.batches.tomtom.dbf.names.Language;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.*;

@Slf4j
@Singleton
public class GeocodeProvider extends TomtomDbfReader {

    private final Map<Long, List<Geocode>> geocodings = newHashMap();

    @Inject
    public GeocodeProvider(TomtomFolder folder) {
        super(folder);
    }

    public void loadGeocodingAttributes(String filename) {
        readFile(filename, (DbfRow row) -> getGeocodings(geocodings, row));
    }

    public Optional<String> getLeftAndRightPostalCode(Long tomtomId) {
        return getFirstGeocodingElement(tomtomId, this::hasLeftOrRightPostalCode, this::getLeftAndRightPostalCode);
    }

    private boolean hasLeftOrRightPostalCode(Geocode geocode) {
        return ofNullable(geocode.getLeftPostalCode()).isPresent() || ofNullable(geocode.getRightPostalCode()).isPresent();
    }

    private String getLeftAndRightPostalCode(Geocode geocode) {
        return of(geocode.getLeftPostalCode()).orElse("") + ";" + of(geocode.getRightPostalCode()).orElse("");
    }

    public Optional<String> getInterpolations(Long tomtomId) {
        return getFirstGeocodingElement(tomtomId, this::hasLeftOrRightInterpolation, this::getInterpolations);
    }

    private Optional<String> getFirstGeocodingElement(Long tomtomId, Predicate<Geocode> filterPredicate, Function<Geocode, String> mapFunction) {
        return ofNullable(geocodings.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(filterPredicate)
                .map(mapFunction)
                .findFirst();
    }

    private String getInterpolations(Geocode geocode) {
        return Interpolation.getOsmValue(geocode.getLeftStructuration()).orElse("") + ";" + Interpolation.getOsmValue(geocode.getRightStructuration()).orElse("");
    }

    private boolean hasLeftOrRightInterpolation(Geocode geocode) {
        return ofNullable(geocode.getLeftStructuration()).filter(this::isInterpolate).isPresent() || ofNullable(geocode.getRightStructuration()).filter(this::isInterpolate).isPresent();
    }

    private boolean isInterpolate(Integer integer) {
        return integer >= 1 && integer <= 6;
    }

    public Map<String, String> getAlternateRoadNamesWithSide(Long tomtomId) {
        return ofNullable(geocodings.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(alternativeName -> ofNullable(alternativeName.getSideOfLine()).isPresent())
                .collect(Collectors.toMap(this::getKeyAlternativeNameWithSide, Geocode::getName, mergeIntoMap()));
    }

    private Optional<String> getSideOfLine(Long side) {
        if (side == 1) {
            return of("left");
        } else if (side == 2) {
            return of("right");
        }
        return empty();
    }

    private String getKeyAlternativeNameWithSide(Geocode geocode) {
        Optional<String> side = getSideOfLine(geocode.getSideOfLine());
        Optional<Language> language = ofNullable(Enums.getIfPresent(Language.class, geocode.getLanguage()).orNull());
        return "name" + language.map(language1 -> ":" + side.map(s -> s + ":").orElse("") + language1.getValue()).orElse(side.map(s -> ":" + s).orElse(""));
    }

    private BinaryOperator<String> mergeIntoMap() {
        return (key1, key2) -> key2;
    }

    private Map<Long, List<Geocode>> getGeocodings(Map<Long, List<Geocode>> geocodes, DbfRow row) {
        Geocode geocode = Geocode.fromDbf(row);
        List<Geocode> geocodingAttributes = geocodings.containsKey(geocode.getId()) ? geocodings.get(geocode.getId()) : newArrayList();
        geocodingAttributes.add(geocode);
        geocodings.put(geocode.getId(), geocodingAttributes);
        return geocodes;
    }

}
