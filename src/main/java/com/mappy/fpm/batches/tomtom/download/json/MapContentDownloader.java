package com.mappy.fpm.batches.tomtom.download.json;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.download.json.model.Content;
import com.mappy.fpm.batches.tomtom.download.json.model.Families.Family;
import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases.Release;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.util.stream.Stream;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static java.util.stream.Collectors.toList;
import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;

public class MapContentDownloader {

    private final FamiliesDownloader familiesDownloader;
    private final ProductsDownloader productsDownloader;
    private final ReleaseDownloader releaseDownloader;
    private final ArchiveDownloader archiveDownloader;

    @Inject
    public MapContentDownloader(FamiliesDownloader familiesDownloader, ProductsDownloader productsDownloader, //
                                ReleaseDownloader releaseDownloader, ArchiveDownloader archiveDownloader) {
        this.familiesDownloader = familiesDownloader;
        this.productsDownloader = productsDownloader;
        this.releaseDownloader = releaseDownloader;
        this.archiveDownloader = archiveDownloader;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Should have 2 arguments : outputFolder, token");
        }

        createInjector(new MapContentModule(args[0], args[1])).getInstance(MapContentDownloader.class).run();
    }

    public void run() {
        Stream<Family> families = familiesDownloader.get();
        Stream<Product> products = families.flatMap(productsDownloader::get);
        Stream<Release> releases = products.flatMap(releaseDownloader::get);

        releases.collect(toList());
        archiveDownloader.download(new Content("eur2016_09-shpd-mn-alb-alb.7z.001", "https://api.tomtom.com/mcapi/contents/2035994"));
    }

    private static class MapContentModule extends AbstractModule {

        private final File outputFolder;
        private final String token;

        private MapContentModule(String outputFolder, String token) {
            this.outputFolder = new File(outputFolder);
            this.outputFolder.mkdirs();
            this.token = token;
        }

        @Override
        protected void configure() {
            bind(HttpClient.class).toInstance(HttpClientBuilder.create().setMaxConnPerRoute(10).setConnectionReuseStrategy(INSTANCE).build());
            bind(File.class).annotatedWith(named("outputFolder")).toInstance(outputFolder);
            bindConstant().annotatedWith(named("token")).to(token);
        }
    }
}
