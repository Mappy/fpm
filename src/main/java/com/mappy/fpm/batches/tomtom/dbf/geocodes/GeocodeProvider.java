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
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.*;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

@Slf4j
@Singleton
public class GeocodeProvider extends TomtomDbfReader {

    private final Map<Long, List<Geocode>> geocodings = newHashMap();

    @Inject
    public GeocodeProvider(TomtomFolder folder) {
        super(folder);
        readFile("gc.dbf", (DbfRow row) -> getGeocodings(geocodings, row));
    }

    public Optional<String> getLeftAndRightPostalCode(Long tomtomId) {
        return getFirstGeocodingElement(tomtomId, this::hasLeftOrRightPostalCode, this::getLeftAndRightPostalCode);
    }

    private boolean hasLeftOrRightPostalCode(Geocode geocode) {
        return ofNullable(geocode.getLeftPostalCode()).isPresent() || ofNullable(geocode.getRightPostalCode()).isPresent();
    }

    private String getLeftAndRightPostalCode(Geocode geocode) {
        return geocode.getLeftPostalCode().equals(geocode.getRightPostalCode()) ? geocode.getLeftPostalCode() : of(geocode.getLeftPostalCode()).orElse("") + ";" + of(geocode.getRightPostalCode()).orElse("");
    }

    public Optional<String> getInterpolationsAddressLeft(Long tomtomId) {
        return getGeocodings(tomtomId).findFirst().map(Geocode::getLeftStructuration).map(Interpolation::getOsmValue) ;
    }

    public Optional<String> getInterpolationsAddressRight(Long tomtomId) {
        return getGeocodings(tomtomId).findFirst().map(Geocode::getRightStructuration).map(Interpolation::getOsmValue) ;
    }

    private Stream<Geocode> getGeocodings(Long tomtomId) {
        return geocodings.getOrDefault(tomtomId, emptyList()).stream();
    }

    public Map<String,String> getInterpolations(Long id) {
        if(geocodings.containsKey(id)){
            Map<String,String> interpolationAddress = new HashMap<>() ;
            Geocode geocode = geocodings.get(id).iterator().next();
            if(isNoneBlank(geocode.getLeftFromAdd())) {
                interpolationAddress.put("interpolation:left" , geocode.getLeftFromAdd() + ";" + geocode.getLeftToAdd() ) ;
            }

            if(isNoneBlank(geocode.getRightFromAdd())) {
                interpolationAddress.put("interpolation:right" , geocode.getRightFromAdd() + ";" + geocode.getRightToAdd()) ;
            }
            return interpolationAddress ;
        }
        return emptyMap();
    }

    private Optional<String> getFirstGeocodingElement(Long tomtomId, Predicate<Geocode> filterPredicate, Function<Geocode, String> mapFunction) {
        return ofNullable(geocodings.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .filter(filterPredicate)
                .map(mapFunction)
                .filter(s -> !s.isEmpty())
                .findFirst();
    }


    public Map<String, String> getAlternateRoadNamesWithSide(Long tomtomId) {
        return getGeocodings(tomtomId)
                .filter(alternativeName -> alternativeName.getSideOfLine() != null)
                .filter(geocode -> Enums.getIfPresent(Language.class, geocode.getLanguage()).isPresent())
                .collect(groupingBy(this::getKeyAlternativeNameWithSide, mapping(Geocode::getName, joining(";") )));
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
        String language = Language.valueOf(geocode.getLanguage()).getValue();
        return "name" + ":" + side.map(s -> s + ":").orElse("") + language ;
    }

    private Map<Long, List<Geocode>> getGeocodings(Map<Long, List<Geocode>> geocodes, DbfRow row) {
        Geocode geocode = Geocode.fromDbf(row);
        List<Geocode> geocodingAttributes = geocodings.containsKey(geocode.getId()) ? geocodings.get(geocode.getId()) : newArrayList();
        geocodingAttributes.add(geocode);
        geocodings.put(geocode.getId(), geocodingAttributes);
        return geocodes;
    }

}
