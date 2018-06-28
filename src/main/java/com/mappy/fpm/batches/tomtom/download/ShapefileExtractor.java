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
import java.nio.file.Files;
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

    public void decompress(File outputDirectory, File file) {
        try {
            SevenZFile archive = new SevenZFile(file);
            Matcher matcher = PATTERN.matcher(file.getName());
            String type = "";
            String country = "";
            if (matcher.matches()) {
                type = matcher.group(3);
                String countryCode = matcher.group(4).toUpperCase();
                if (outerworld().stream().anyMatch(c -> c.getLabel().equals(countryCode)) && "ax".equals(matcher.group(5))) {
                    return;
                }
                try {
                    country = COUNTRIES.stream().filter(c -> c.getId().equals(countryCode)).findFirst().get().getLabel();
                } catch (java.util.NoSuchElementException e) {
                    log.error("Unknown country with countryCode: " + countryCode);
                    country = countryCode;
                }
            }

            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().contains(".gz")) {
                    continue;
                }
                String filename = Paths.get(entry.getName()).getFileName().toString();
                log.info("Extracting {}", filename);
                File currentFile = new File(outputDirectory, entry.getName().replace(".gz", ""));
                File parent = currentFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                byte[] content = new byte[(int) entry.getSize()];
                archive.read(content, 0, content.length);
                try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(content)); FileOutputStream output = new FileOutputStream(currentFile)) {
                    IOUtils.copy(input, output);
                }

                // Creating a symlink to the files that fpm uses
                if (allTomtomFiles(type).stream().anyMatch(filename::contains)) {
                    File countryDirectory = new File(outputDirectory, country);
                    countryDirectory.mkdirs();
                    File outputFile = new File(countryDirectory, filename.replace(".gz", ""));
                    if (outputFile.exists()) {
                        outputFile.delete();
                    }
                    Files.createSymbolicLink(outputFile.toPath(), Paths.get(currentFile.getAbsolutePath()));
                }
            }
            archive.close();
        } catch (IOException e) {
            throw propagate(e);
        } finally {
            file.delete();
        }
    }
}