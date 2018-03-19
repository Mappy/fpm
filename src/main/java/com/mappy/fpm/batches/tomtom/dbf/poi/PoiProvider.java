package com.mappy.fpm.batches.tomtom.dbf.poi;

import com.google.common.collect.ImmutableList;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import com.mappy.fpm.batches.tomtom.dbf.TomtomDbfReader;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Slf4j
@Singleton
public class PoiProvider extends TomtomDbfReader {

    private final Map<Long, Poi> poiNames = new HashMap<>();
    private final Map<Long, List<String>> poiExtendedAttributes = new HashMap<>();

    @Inject
    public PoiProvider(TomtomFolder folder) {
        super(folder);
        readFile("pi.dbf", row -> poiNames.put(row.getLong("CLTRPELID"), Poi.fromDbf(row)));
        readFile("piea.dbf", this::getExtendedAttributes);
    }

    public Optional<String> getPoiNameByType(Long id, String type) {
        return isType(id, type) ? of(poiNames.get(id).getName()) : empty();
    }

    private Boolean isType(Long id, String type) {
        return ofNullable(poiNames.get(id)).isPresent() && ofNullable(poiExtendedAttributes.get(poiNames.get(id).getId()))
                .orElse(ImmutableList.of())
                .stream()
                .anyMatch(t -> t.equals(type));

    }

    private void getExtendedAttributes(DbfRow row) {
        Long id = row.getLong("ID");
        List<String> geocodingAttributes = poiExtendedAttributes.containsKey(id) ? poiExtendedAttributes.get(id) : newArrayList();
        geocodingAttributes.add(row.getString("ATTTYP"));
        poiExtendedAttributes.put(id, geocodingAttributes);
    }
}

