package com.mappy.fpm.batches.tomtom.download.json;

import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.download.ShapefileExtractor;
import com.mappy.fpm.batches.tomtom.download.json.downloader.*;

import javax.inject.Named;
import java.io.File;

import static com.google.inject.Guice.createInjector;

public class MapContentDownloader {
    private final FamiliesDownloader familiesDownloader;
    private final ProductsDownloader productsDownloader;
    private final ReleaseDownloader releaseDownloader;
    private final ContentDownloader contentDownloader;
    private final DirectUrlDownloader directUrlDownloader;
    private final ArchiveDownloader archiveDownloader;
    private final ShapefileExtractor shapefileExtractor;
    private final File outputFolder;

    @Inject
    public MapContentDownloader(FamiliesDownloader familiesDownloader, ProductsDownloader productsDownloader, //
                                ReleaseDownloader releaseDownloader, ContentDownloader contentDownloader, //
                                DirectUrlDownloader directUrlDownloader, ArchiveDownloader archiveDownloader, //
                                ShapefileExtractor shapefileExtractor, @Named("outputFolder") File outputFolder) {
        this.familiesDownloader = familiesDownloader;
        this.productsDownloader = productsDownloader;
        this.releaseDownloader = releaseDownloader;
        this.contentDownloader = contentDownloader;
        this.directUrlDownloader = directUrlDownloader;
        this.archiveDownloader = archiveDownloader;
        this.shapefileExtractor = shapefileExtractor;
        this.outputFolder = outputFolder;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("Should have 3 arguments : outputFolder, token, version");
        }

        createInjector(new MapContentModule(args[0], args[1], args[2])).getInstance(MapContentDownloader.class).run();
    }

    public void run() {
        familiesDownloader.get()//
                .parallel()//
                .flatMap(productsDownloader)//
                .flatMap(releaseDownloader)//
                .flatMap(contentDownloader)//
                .map(directUrlDownloader)//
                .map(archiveDownloader)//
                .forEach(file -> shapefileExtractor.decompress(outputFolder, file));
    }
}
