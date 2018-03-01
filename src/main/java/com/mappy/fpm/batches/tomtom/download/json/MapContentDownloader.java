package com.mappy.fpm.batches.tomtom.download.json;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.download.json.model.Content;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;

public class MapContentDownloader {

    private final ContentDownloader contentDownloader;

    @Inject
    public MapContentDownloader(ContentDownloader contentDownloader) {
        this.contentDownloader = contentDownloader;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Should have 2 arguments : outputFolder, token");
        }

        createInjector(new MapContentModule(args[0], args[1])).getInstance(MapContentDownloader.class).run();
    }

    public void run() {
        contentDownloader.download(new Content("eur2018_03-shpd-mn-and-and.7z.001", "https://api.tomtom.com/mcapi/contents/1968603"));
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
