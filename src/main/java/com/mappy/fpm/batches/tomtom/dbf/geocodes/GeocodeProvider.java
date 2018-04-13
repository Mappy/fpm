package com.mappy.fpm.batches.tomtom.dbf.geocodes;

import com.google.common.base.Enums;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import com.mappy.fpm.batches.tomtom.dbf.names.Language;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.*;
import static java.util.stream.Collectors.*;

@Slf4j
@Singleton
public class GeocodeProvider extends TomtomDbfReader {

    public static final int OFFICIAL_NAME = 1;
    private final Map<Long, List<Geocode>> geocodings = newHashMap();

    @Inject
    public GeocodeProvider(TomtomFolder folder) {
        super(folder);
        readFile("gc.dbf", (DbfRow row) -> getGeocodings(geocodings, row));
    }

    public Optional<String> getLeftPostalCode(Long tomtomId) {
        return getGeocodings(tomtomId).findFirst().map(Geocode::getLeftPostalCode).filter(StringUtils::isNotEmpty) ;
    }

    public Optional<String> getRightPostalCode(Long tomtomId) {
        return getGeocodings(tomtomId).findFirst(). map(Geocode::getRightPostalCode).filter(StringUtils::isNotEmpty) ;
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
            if(geocode.getLeftFromAdd() != null &&  geocode.getLeftFromAdd() != -1) {
                interpolationAddress.put("interpolation:left" , geocode.getLeftFromAdd() + ";" + geocode.getLeftToAdd() ) ;
            }

            if(geocode.getRightFromAdd() != null && geocode.getRightFromAdd() != -1) {
                interpolationAddress.put("interpolation:right" , geocode.getRightFromAdd() + ";" + geocode.getRightToAdd()) ;
            }
            return interpolationAddress ;
        }
        return emptyMap();
    }

    public Map<String, String> getNamesAndAlternateNamesWithSide(Long tomtomId) {

        return getGeocodings(tomtomId)
                .filter(alternativeName -> alternativeName.getSideOfLine() != null)
                .sorted(Comparator.comparing(this::getMinBitMask))
                .collect(groupingBy(this::getKeyAlternativeNameWithSide, mapping(Geocode::getName, joining(";") )));
    }

    private String getSideOfLine(Long side) {
        if (side == 1) {
            return "left";
        } else if (side == 2) {
            return "right";
        }
        return null;
    }

    private String getKeyAlternativeNameWithSide(Geocode geocode) {
        String side = getSideOfLine(geocode.getSideOfLine());
        String language = geocode.getLanguage().getValue();
        return  Stream.of(getNameTag(geocode), side, language).filter(Objects::nonNull).collect(joining(":")) ;
    }

    private Map<Long, List<Geocode>> getGeocodings(Map<Long, List<Geocode>> geocodes, DbfRow row) {
        Geocode geocode = Geocode.fromDbf(row);
        List<Geocode> geocodingAttributes = geocodings.containsKey(geocode.getId()) ? geocodings.get(geocode.getId()) : newArrayList();
        geocodingAttributes.add(geocode);
        geocodings.put(geocode.getId(), geocodingAttributes);
        return geocodes;
    }

    private int getMinBitMask(Geocode geocode) {
        return IntStream.of(1,2,4,8,16,32,64).filter(mask -> (geocode.getType() & mask) == mask).findFirst().orElse(MAX_VALUE)  ;
    }

    private String getNameTag(Geocode geocode) {
        return getMinBitMask(geocode) != OFFICIAL_NAME || geocode.getLanguage() == Language.UND ? "alt_name" : "name" ;
    }

}
