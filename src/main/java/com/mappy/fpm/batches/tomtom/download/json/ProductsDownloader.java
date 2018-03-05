package com.mappy.fpm.batches.tomtom.download.json;

import com.google.gson.Gson;
import com.mappy.fpm.batches.tomtom.download.json.model.Families.Family;
import com.mappy.fpm.batches.tomtom.download.json.model.Products;
import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import static com.google.common.base.Throwables.propagate;

public class ProductsDownloader {

    private final HttpClient client;
    private final String token;

    @Inject
    public ProductsDownloader(HttpClient client, @Named("token") String token) {
        this.client = client;
        this.token = token;
    }

    public Stream<Product> get(Family family) {
        HttpGet get = new HttpGet(family.getLocation() + "/products");
        get.addHeader("Authorization", token);
        try {
            InputStream response = client.execute(get).getEntity().getContent();
            return new Gson().fromJson(IOUtils.toString(response, "UTF-8"), Products.class).getContent().stream();
        } catch (IOException e) {
            throw propagate(e);
        }
    }
}
