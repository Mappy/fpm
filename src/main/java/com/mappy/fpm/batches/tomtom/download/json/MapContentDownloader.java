package com.mappy.fpm.batches.tomtom.download.json;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;

public class MapContentDownloader {

    private final FamiliesDownloader familiesDownloader;
    private final ProductsDownloader productsDownloader;
    private final ReleaseDownloader releaseDownloader;
    private final ContentDownloader contentDownloader;
    private final ArchiveDownloader archiveDownloader;

    @Inject
    public MapContentDownloader(FamiliesDownloader familiesDownloader, ProductsDownloader productsDownloader, //
                                ReleaseDownloader releaseDownloader, ContentDownloader contentDownloader, //
                                ArchiveDownloader archiveDownloader) {
        this.familiesDownloader = familiesDownloader;
        this.productsDownloader = productsDownloader;
        this.releaseDownloader = releaseDownloader;
        this.contentDownloader = contentDownloader;
        this.archiveDownloader = archiveDownloader;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("Should have 3 arguments : outputFolder, token, version");
        }

        createInjector(new MapContentModule(args[0], args[1], args[2])).getInstance(MapContentDownloader.class).run();
    }

    public void run() {

        familiesDownloader.get()//
                .parallel()
                .flatMap(productsDownloader)//
                .flatMap(releaseDownloader)//
                .flatMap(contentDownloader)//
                .forEach(archiveDownloader::download);
    }

    private static class MapContentModule extends AbstractModule {

        private final File outputFolder;
        private final String version;
        private final String token;

        private MapContentModule(String outputFolder, String token, String version) {
            this.outputFolder = new File(outputFolder);
            this.outputFolder.mkdirs();
            this.version = version;
            this.token = token;
        }

        @Override
        protected void configure() {
            bind(HttpClient.class).toInstance(HttpClientBuilder.create().setMaxConnPerRoute(10).setConnectionReuseStrategy(INSTANCE).build());
            bind(File.class).annotatedWith(named("outputFolder")).toInstance(outputFolder);
            bindConstant().annotatedWith(named("token")).to(token);
            bindConstant().annotatedWith(named("version")).to(version);
        }
    }
}
