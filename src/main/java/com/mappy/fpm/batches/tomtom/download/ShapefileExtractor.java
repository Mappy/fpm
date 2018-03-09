package com.mappy.fpm.batches.tomtom.download;

import com.mappy.fpm.batches.tomtom.download.TomtomCountries.TomtomCountry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Sets.union;
import static com.mappy.fpm.batches.tomtom.download.TomtomCountries.countries;
import static com.mappy.fpm.batches.tomtom.download.TomtomCountries.outerworld;
import static com.mappy.fpm.batches.tomtom.download.TomtomFile.allTomtomFiles;
import static com.mappy.fpm.batches.tomtom.download.json.downloader.ContentDownloader.PATTERN;

@Slf4j
public class ShapefileExtractor {

    private static final Set<TomtomCountry> COUNTRIES = union(countries(), outerworld());

    public static void decompress(File outputDirectory, File file, String type) {
        try {
            SevenZFile archive = new SevenZFile(file);
            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                String filename = Paths.get(entry.getName()).getFileName().toString();

                if (tablesNeeded(type).stream().anyMatch(filename::contains)) {
                    log.info("Extracting {}", filename);
                    byte[] content = new byte[(int) entry.getSize()];
                    archive.read(content, 0, content.length);
                    File outputFile = new File(outputDirectory, filename.replace(".gz", ""));
                    try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(content)); FileOutputStream output = new FileOutputStream(outputFile)) {
                        IOUtils.copy(input, output);
                    }
                }
            }
            archive.close();
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    public void decompress(File outputDirectory, File file) {
        try {
            SevenZFile archive = new SevenZFile(file);
            Matcher matcher = PATTERN.matcher(file.getName());
            String type = "";
            String country = "";
            if (matcher.matches()){
                type = matcher.group(3);
                String countryCode = matcher.group(4).toUpperCase();
                if (outerworld().stream().anyMatch(c -> c.getLabel().equals(countryCode)) && "ax".equals(matcher.group(5))) {
                    return;
                }
                country = COUNTRIES.stream().filter(c -> c.getId().equals(countryCode)).findFirst().get().getLabel();
            }

            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                String filename = Paths.get(entry.getName()).getFileName().toString();

                if (tablesNeeded(type).stream().anyMatch(filename::contains)) {
                    log.info("Extracting {}", filename);
                    byte[] content = new byte[(int) entry.getSize()];
                    archive.read(content, 0, content.length);
                    File countryDirectory = new File(outputDirectory, country);
                    countryDirectory.mkdirs();
                    File outputFile = new File(countryDirectory, filename.replace(".gz", ""));
                    try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(content)); FileOutputStream output = new FileOutputStream(outputFile)) {
                        IOUtils.copy(input, output);
                    }
                }
            }
            archive.close();
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private static List<String> tablesNeeded(String type) {
        List<String> allTomtomFiles = allTomtomFiles(type);

        if (allTomtomFiles.isEmpty()) {
            throw new IllegalStateException();
        }

        return allTomtomFiles;
    }
}