package com.mappy.fpm.batches.tomtom.download.json;

import com.google.inject.AbstractModule;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;

import static com.google.inject.name.Names.named;
import static org.apache.http.impl.NoConnectionReuseStrategy.INSTANCE;

public class MapContentModule extends AbstractModule {

    private final File outputFolder;
    private final String version;
    private final String token;

    public MapContentModule(String outputFolder, String token, String version) {
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