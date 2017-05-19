package com.mappy.data.batches.geonames;

import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Singleton
public class Geonames {

    private final TreeMultimap<Integer, AlternateName> alternateNames;
    private final Map<String, Integer> idByCountry;

    @Inject
    public Geonames(@Named("com.mappy.data.geonames") String path) {
        alternateNames = alternateNames(path + "/alternateNames.txt");
        idByCountry = idByCountry(path + "/countryInfo.txt");
    }

    public List<AlternateName> frenchNames(String isocode) {
        return idByCountry.containsKey(isocode) ? frenchNames(idByCountry.get(isocode)) : newArrayList();
    }

    public List<AlternateName> frenchNames(int id) {
        return newArrayList(alternateNames.get(id));
    }

    private static Map<String, Integer> idByCountry(String path) {
        Map<String, Integer> idByCountry = Maps.newHashMap();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if (!line.startsWith("#")) {
                    List<String> list = Splitter.on('\t').splitToList(line);
                    idByCountry.put(list.get(1), parseInt(list.get(16)));
                }
            }
        }
        catch (IOException e) {
            throw propagate(e);
        }
        return idByCountry;
    }

    private static TreeMultimap<Integer, AlternateName> alternateNames(String path) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        TreeMultimap<Integer, AlternateName> multimap = TreeMultimap.create();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                List<String> list = Splitter.on('\t').splitToList(line);
                if ("fr".equals(list.get(2))) {
                    AlternateName name = new AlternateName(list.get(3), "1".equals(list.get(4)), "1".equals(list.get(5)), "1".equals(list.get(6)), "1".equals(list.get(7)));
                    multimap.put(parseInt(list.get(1)), name);
                }
            }
        }
        catch (IOException e) {
            throw propagate(e);
        }
        log.info("Alternate names loaded: {}s", stopwatch.elapsed(SECONDS));
        return multimap;
    }
}