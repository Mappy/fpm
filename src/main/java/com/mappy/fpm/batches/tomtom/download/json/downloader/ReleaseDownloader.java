package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases.Release;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;

@Slf4j
public class ReleaseDownloader implements Function<Product, Stream<Release>> {

    private final String version;
    private final HttpClient client;
    private final String token;

    @Inject
    public ReleaseDownloader(@Named("version") String version, HttpClient client, @Named("token") String token) {
        this.version = version;
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Release> apply(Product product) {
        log.info("Get version {} from {}", version, product.getName());
        HttpGet get = new HttpGet(product.getLocation() + "/releases");
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {
            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Releases.class).getContent().stream() //
                    .filter(r -> version.equals(r.getVersion()))
                    .filter(Objects::nonNull);
        } catch (IOException e) {
            throw propagate(e);
        }
    }
}
