package com.mappy.fpm.batches.tomtom.download.json;

import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases.Release;
import org.apache.http.client.HttpClient;

import javax.inject.Named;
import java.util.stream.Stream;

public class ReleaseDownloader {

    private final HttpClient client;
    private final String token;

    public ReleaseDownloader(HttpClient client, @Named("token") String token) {

        this.client = client;
        this.token = token;
    }

    public Stream<Release> get(Product product) {
        return null;
    }
}
