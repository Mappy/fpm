package com.mappy.fpm.batches.tomtom.dbf.names;

import com.google.common.base.Stopwatch;
import com.mappy.fpm.batches.tomtom.TomtomFolder;
import lombok.extern.slf4j.Slf4j;
import org.jamel.dbf.DbfReader;
import org.jamel.dbf.structure.DbfRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@Singleton
public class NameProvider {

    private final Map<Long, List<AlternativeName>> alternateNames = newHashMap();
    private final Map<Long, List<AlternativeName>> alternateCityNames = newHashMap();
    private final TomtomFolder folder;

    @Inject
    public NameProvider(TomtomFolder folder) {
        this.folder = folder;
    }

    public void loadFromFile(String filename, String alternativeParamName, boolean hasSideName) {
        readFile(filename, alternativeParamName, hasSideName, this.alternateNames);
    }

    public void loadFromCityFile(String filename, String alternativeParamName, boolean hasSideName) {
        readFile(filename, alternativeParamName, hasSideName, this.alternateCityNames);
    }

    private void readFile(String filename, String alternativeParamName, boolean hasSideName, Map<Long, List<AlternativeName>> alternateCityNames) {
        File file = new File(folder.getFile(filename));
        if (file.exists()) {
            log.info("Opening {}", file);
            try (DbfReader reader = new DbfReader(file)) {
                DbfRow row;
                Stopwatch stopwatch = Stopwatch.createStarted();
                int counter = 0;
                while ((row = reader.nextRow()) != null) {
                    AlternativeName altName = AlternativeName.fromDbf(row, alternativeParamName, hasSideName);
                    List<AlternativeName> altNames = alternateCityNames.containsKey(altName.getId()) ? alternateCityNames.get(altName.getId()) : newArrayList();

                    altNames.add(altName);
                    alternateCityNames.put(altName.getId(), altNames);
                    counter++;
                }
                long time = stopwatch.elapsed(MILLISECONDS);
                stopwatch.stop();
                log.info("Added {} object(s){}", counter, counter > 0 ? " in " + time + " ms at rate " + String.format("%.2f", counter * 1.0 / time) + " obj/ms" : "");
            }
        }
        else {
            log.info("File not found : {}", file.getAbsolutePath());
        }
    }

    public Map<String, String> getAlternateNames(Long tomtomId) {
        return getNames(tomtomId, alternateNames);
    }

    public Map<String, String> getAlternateCityNames(Long tomtomId) {
        return getNames(tomtomId, alternateCityNames);
    }

    private Map<String, String> getNames(Long tomtomId, Map<Long, List<AlternativeName>> alternateNames) {
        Map<String, String> tags = newHashMap();
        List<AlternativeName> an = alternateNames.get(tomtomId);
        if (an != null) {
            an.forEach(alternativeName -> {
                try {
                    tags.put("name:" + Language.valueOf(alternativeName.getLanguage()).getValue(), alternativeName.getName());
                }
                catch (IllegalArgumentException e) {
                    tags.put("alt_name", alternativeName.getName());
                }
            });
        }
        return tags;
    }

    public Map<String, String> getSideNames(Long tomtomId, Integer sol) {
        Map<String, String> tags = newHashMap();
        if (sol != null && sol != 0) {
            List<AlternativeName> alternativeNames = alternateNames.get(tomtomId);
            if (alternativeNames != null) {
                alternativeNames.forEach(alternativeName -> {
                    if (alternativeName.getSideOfLine() != null) {
                        if (alternativeName.getSideOfLine() == 1) {
                            tags.put("name:left", alternativeName.getName());
                            try {
                                tags.put("name:left:" + Language.valueOf(alternativeName.getLanguage()).getValue(), alternativeName.getName());
                            }
                            catch (IllegalArgumentException e) {}
                        }
                        else if (alternativeName.getSideOfLine() == 2) {
                            tags.put("name:right", alternativeName.getName());
                            try {
                                tags.put("name:right:" + Language.valueOf(alternativeName.getLanguage()).getValue(), alternativeName.getName());
                            }
                            catch (IllegalArgumentException e) {}
                        }
                    }
                });
            }
        }
        return tags;
    }
}
