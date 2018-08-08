package com.mappy.fpm.batches;

import com.google.common.annotations.VisibleForTesting;
import com.mappy.fpm.batches.merge.pbf.OsmMerger;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory.Builder;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Splitter.*;
import static com.google.common.base.Throwables.*;
import static com.google.common.collect.Lists.*;
import static com.google.inject.Guice.*;
import static com.mappy.fpm.batches.CountryWrapper.*;
import static java.lang.Integer.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateFullPbf {
    public static final String OSM_SUFFIX = ".osm.pbf";
    private static final String TOWN_SUFFIX = "_2dtb.shp";
    private static final String COUNTRY_SUFFIX = "______________a0.shp";
    private static final String FERRY_SUFFIX = "___________fe.shp";
    private static final String ROAD_SUFFIX = "___________nw.shp";

    private final OsmMerger osmMerger;
    private final String inputDirectoryPath;
    private final String outputDirectoryPath;
    private final String outputFileName;
    private final ExecutorService executorService;

    public GenerateFullPbf(OsmMerger osmMerger, String inputDirectoryPath, String outputDirectoryPath, String outputFileName, int nbThreads) {
        this.osmMerger = osmMerger;
        this.inputDirectoryPath = inputDirectoryPath;
        this.outputDirectoryPath = outputDirectoryPath;
        this.outputFileName = outputFileName;
        BasicThreadFactory threadFactory = new Builder().namingPattern("mappy-GenerateFullPbf-%d").daemon(false).build();
        executorService = new ThreadPoolExecutor(nbThreads, nbThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
    }

    public static void main(String[] args) {
        checkArgument(args.length == 5, "Usage: GenerateFullPbf <countryList> <inputDirectoryPath> <outputDirectoryPath> <outputFileName> <threadNumber>");

        List<String> countries = checkAndValidCountries(args[0]);
        String inputDirectoryPath = args[1];
        String outputDirectoryPath = args[2];
        String outputFileName = args[3];
        String threadNumber = args[4];

        new GenerateFullPbf( //
                new OsmMerger(), //
                inputDirectoryPath, //
                outputDirectoryPath, //
                outputFileName, //
                parseInt(threadNumber)).run(countries);
    }

    public void run(List<String> countries) {
        log.info("Running with countries : {}", countries);

        try {
            List<String> countryPbfFiles = countries.stream().map(this::generateCountry).collect(toList());
            mergePbfFiles(countryPbfFiles, outputDirectoryPath + "/" + outputFileName, newArrayList());

        }
        finally {
            log.info("Shutting down service...");
            executorService.shutdown();
        }
    }

    @VisibleForTesting
    static List<String> checkAndValidCountries(String countryList) {
        List<String> countries = on(",").trimResults().splitToList(countryList).stream().filter(StringUtils::isNotBlank).collect(toList());
        String invalidCountries = countries.stream().filter(StringUtils::isNotBlank).filter(country -> !ALL_COUNTRIES.contains(country)).collect(joining(", "));
        checkArgument(invalidCountries.isEmpty(), "Invalid countries : " + invalidCountries + "\n" + ALL_COUNTRIES.stream().collect(joining(", ", "Valid countries [", "]")));

        if (countries.isEmpty()) {
            // return ALL_COUNTRIES.stream().collect(toList());
            String msg = "No country provided.\nPlease list the countries to process in " + ALL_COUNTRIES.stream().collect(joining(", ", "[", "]"));
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        return countries;
    }

    @NotNull
    private String generateCountry(String country) {
        log.info("Generating country : {}", country);

        File file = new File(inputDirectoryPath + "/" + country);
        if (!file.exists()) {
            String msg = format("No input file for country : %s.", country);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        List<String> zonePbfFiles = newArrayList();
        List<Future<?>> zonesFutures = newArrayList();

        for (String zoneFileName : of(file.list()).filter(f -> f.endsWith(TOWN_SUFFIX) || f.endsWith(ROAD_SUFFIX) || f.endsWith(FERRY_SUFFIX) || f.endsWith(COUNTRY_SUFFIX)).collect(toList())) {

            String zone = zoneFileName.replace(TOWN_SUFFIX, "").replace(ROAD_SUFFIX, "").replace(FERRY_SUFFIX, "").replace(COUNTRY_SUFFIX, "");
            Tomtom2Osm instance = createInjector(
                    new Tomtom2OsmModule(inputDirectoryPath + "/" + country + "/", outputDirectoryPath + "/" + country + "/pbfFiles", outputDirectoryPath + "/splitter", zone))
                            .getInstance(Tomtom2Osm.class);

            Future<?> zoneFuture = executorService.submit(() -> {
                try {
                    Optional<String> OSMZone = instance.run();
                    OSMZone.ifPresent(zonePbfFiles::add);
                }
                catch (IOException e) {
                    log.info("Error when generating zone: {}", zone, e);
                    propagate(e);
                }
            });
            zonesFutures.add(zoneFuture);
        }

        String countryFile = outputDirectoryPath + "/" + country + "/" + country + OSM_SUFFIX;
        mergePbfFiles(zonePbfFiles, countryFile, zonesFutures);

        log.info("Done generating country : {}", country);
        return countryFile;
    }

    private void mergePbfFiles(List<String> inputPbfFiles, String outputFile, List<Future<?>> tasksToWaitFor) {
        try {
            waitAllDone(tasksToWaitFor);
            osmMerger.merge(inputPbfFiles, outputFile);
        }
        catch (IOException e) {
            propagate(e);
        }
    }

    private void waitAllDone(List<Future<?>> tasks) {
        tasks.forEach(t -> {
            try {
                t.get();
            }
            catch (ExecutionException | InterruptedException e) {
                propagate(e);
            }
        });
    }
}
