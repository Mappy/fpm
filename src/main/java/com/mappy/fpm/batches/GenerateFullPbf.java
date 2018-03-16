package com.mappy.fpm.batches;

import com.mappy.fpm.batches.merge.pbf.OsmMerger;
import com.mappy.fpm.batches.tomtom.Tomtom2Osm;
import com.mappy.fpm.batches.tomtom.Tomtom2OsmModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory.Builder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Splitter.on;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.inject.Guice.createInjector;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

@Slf4j
public class GenerateFullPbf {
    public static final String OSM_SUFFIX = ".osm.pbf";
    private static final String TOWN_SUFFIX = "_2dbd.shp";
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

        String countryList = args[0];
        String inputDirectoryPath = args[1];
        String outputDirectoryPath = args[2];
        String outputFileName = args[3];
        String threadNumber = args[4];

        new GenerateFullPbf( //
                new OsmMerger(), //
                inputDirectoryPath, //
                outputDirectoryPath, //
                outputFileName, //
                parseInt(threadNumber)).run(on(",").trimResults().splitToList(countryList));
    }

    public void run(List<String> countries) {
        log.info("Running with countries : {}", countries);

        try {
            List<String> countryPbfFiles = countries.stream()
                    .filter(this::hasCountryInFS) //
                    .map(this::generateCountry) //
                    .collect(toList());

            if (countryPbfFiles.isEmpty()) {
                log.warn("No country processed!");
                return;
            }
            mergePbfFiles(countryPbfFiles, outputDirectoryPath + "/" + outputFileName, newArrayList());
        } finally {
            log.info("Shutting down service...");
            executorService.shutdown();
        }
    }

    @NotNull
    private String generateCountry(String country) {
        log.info("Generating country : {}", country);

        File file = new File(inputDirectoryPath + "/" + country);

        List<String> zonePbfFiles = newArrayList();
        List<Future<?>> zonesFutures = newArrayList();

        for (String zoneFileName : of(file.list()).filter(f -> f.endsWith(TOWN_SUFFIX) || f.endsWith(ROAD_SUFFIX) || f.endsWith(FERRY_SUFFIX) || f.endsWith(COUNTRY_SUFFIX)).collect(toList())) {
            String zone = zoneFileName.replace(TOWN_SUFFIX, "").replace(ROAD_SUFFIX, "").replace(FERRY_SUFFIX, "").replace(COUNTRY_SUFFIX, "");

            Tomtom2Osm instance = createInjector(new Tomtom2OsmModule(
                    inputDirectoryPath + "/" + country + "/",
                    outputDirectoryPath + "/" + country + "/pbfFiles",
                    outputDirectoryPath + "/splitter",
                    zone)).getInstance(Tomtom2Osm.class);

            Future<?> zoneFuture = executorService.submit(() -> {
                try {
                    Optional<String> OSMZone = instance.run();
                    OSMZone.ifPresent(zonePbfFiles::add);
                } catch (IOException e) {
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
        } catch (IOException e) {
            propagate(e);
        }
    }

    private void waitAllDone(List<Future<?>> tasks) {
        tasks.forEach(t -> {
            try {
                t.get();
            } catch (ExecutionException | InterruptedException e) {
                propagate(e);
            }
        });
    }

    private boolean hasCountryInFS(String country) {
        if (Files.exists(Paths.get(inputDirectoryPath + "/" + country))) {
            return true;
        }
        log.warn(format("No input file for country : %s. Skipping...", country));
        return false;
    }
}
