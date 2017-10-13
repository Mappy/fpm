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
            SevenZArchiveEntry entry = null;
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
        }
        catch (IOException e) {
            throw propagate(e);
        }
    }

    private static List<String> tablesNeeded(boolean outerworld, String type) {
        if (outerworld) {
            return newArrayList("nw.");
        }
        if ("mn".equals(type)) {
            return newArrayList(
                    "_nw.", // roads
                    "_rs.", // road restrictions
                    "_mn.", // road maneuver
                    "_mp.", // road maneuver
                    "_lc.", // land covers
                    "_lu.", // land uses
                    "_wa.", // water areas
                    "_wl.", // water lines
                    "_sm.", // cities
                    "_smnm.", // alternate city names
                    "_bl.", // coastlines
                    "_rr.", // railroads
                    "_gc.", // Geocoding Information (alternate roads names)
                    "_sr.", // speeds restrictions (for maxspeed)
                    "_si.", // sign post information
                    "_sp.", // sign post paths
                    "_sg.", // sign post
                    "_ld.", // lane directions
                    "_a0.", // country boundaries
                    "_a1.",
                    "_a2.",
                    "_a3.",
                    "_a4.",
                    "_a5.",
                    "_a6.",
                    "_a7.",
                    "_oa07.", // extended cities boundaries
                    "_a8.", // cities boundaries
                    "_a9.",
                    "_an.", // alternate names
                    "_bu.", // built-up area
                    "_td." // time domains
            );
        }
        else if ("sp".equals(type)) {
            return newArrayList("_hsnp.", "_hspr.");
        }
        else if ("2dcmnb".equals(type)) {
            return newArrayList("_2dbd.", "_2dtb.");
        }
        throw new IllegalStateException();
    }
}