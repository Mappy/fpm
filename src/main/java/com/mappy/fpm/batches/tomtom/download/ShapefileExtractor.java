package com.mappy.fpm.batches.tomtom.download;

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
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;

@Slf4j
public class ShapefileExtractor {

    public static void decompress(File outputDirectory, File file, boolean outerworld, String type) {
        try {
            SevenZFile archive = new SevenZFile(file);
            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                String filename = Paths.get(entry.getName()).getFileName().toString();

                if (tablesNeeded(outerworld, type).stream().anyMatch(filename::contains)) {
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

    private static List<String> tablesNeeded(boolean outerworld, String type) {
        if (outerworld) {
            return newArrayList("nw.");
        }

        List<String> allTomtomFiles = TomtomFile.allTomtomFiles(type);

        if (allTomtomFiles.isEmpty()) {
            throw new IllegalStateException();
        }

        return allTomtomFiles;
    }
}