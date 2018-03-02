package com.mappy.fpm.batches.tomtom.dbf.names;

import com.google.common.base.Enums;
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
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.ofNullable;

@Slf4j
@Singleton
public class NameProvider extends TomtomDbfReader {

    private final Map<Long, List<AlternativeName>> alternateNames = newHashMap();
    private final Map<Long, List<AlternativeName>> alternateCityNames = newHashMap();

    @Inject
    public NameProvider(TomtomFolder folder) {
        super(folder);
    }

    public void loadAlternateNames(String filename) {
        readFile(filename, dbfRow -> getAlternateNames(alternateNames, dbfRow));
    }

    public void loadAlternateCityNames(String filename) {
        readFile(filename, dbfRow -> getAlternateNames(alternateCityNames, dbfRow));
    }

    public Map<String, String> getAlternateCityNames(Long tomtomId) {
        return getAlternateNames(tomtomId, alternateCityNames);
    }

    public Map<String, String> getAlternateNames(Long tomtomId) {
        return getAlternateNames(tomtomId, alternateNames);
    }

    private Map<String, String> getAlternateNames(Long tomtomId, Map<Long, List<AlternativeName>> alternateNames) {
        return ofNullable(alternateNames.get(tomtomId))
                .orElse(ImmutableList.of())
                .stream()
                .collect(Collectors.toMap(this::getKeyAlternativeName, AlternativeName::getName, mergeIntoMap()));
    }

    private String getKeyAlternativeName(AlternativeName alternativeName) {
        String keyPrefix = "ON".equals(alternativeName.getType()) ? "name:" : "alt_name:";
        Optional<Language> language = ofNullable(Enums.getIfPresent(Language.class, alternativeName.getLanguage()).orNull());
        return language.map(language1 -> keyPrefix + language1.getValue()).orElse("int_name");
    }

    private BinaryOperator<String> mergeIntoMap() {
        return (key1, key2) -> key2;
    }

    private void getAlternateNames(Map<Long, List<AlternativeName>> alternates, DbfRow row) {
        AlternativeName altName = AlternativeName.fromDbf(row);
        List<AlternativeName> altNames = alternates.containsKey(altName.getId()) ? alternates.get(altName.getId()) : newArrayList();
        altNames.add(altName);
        alternates.put(altName.getId(), altNames);
    }
}
