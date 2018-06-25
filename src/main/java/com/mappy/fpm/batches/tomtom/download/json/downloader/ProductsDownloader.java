package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.google.gson.Gson;
import com.mappy.fpm.batches.tomtom.download.json.model.Families.Family;
import com.mappy.fpm.batches.tomtom.download.json.model.Products;
import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;

@Slf4j
public class ProductsDownloader implements Function<Family, Stream<Product>> {

    private final HttpClient client;
    private final String token;

    @Inject
    public ProductsDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    @Override
    public Stream<Product> apply(Family family) {
        log.info("Get all products from {}", family.getAbbreviation());
        HttpGet get = new HttpGet(family.getLocation() + "/products");
        get.addHeader("Authorization", token);

        try (InputStream response = client.execute(get).getEntity().getContent()) {
            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Products.class).getContent().stream();
        } catch (IOException e) {
            throw propagate(e);
        }
    }
}
