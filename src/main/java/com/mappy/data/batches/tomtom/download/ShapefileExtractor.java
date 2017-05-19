package com.mappy.data.batches.tomtom.download;

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

@Slf4j
public class ShapefileExtractor {
    public static void decompress(File outputDirectory, File file, List<String> patterns) {
        try {
            SevenZFile archive = new SevenZFile(file);
            SevenZArchiveEntry entry = null;
            while ((entry = archive.getNextEntry()) != null) {
                String filename = Paths.get(entry.getName()).getFileName().toString();

                if (patterns.stream().anyMatch(filename::contains)) {
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
}