package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.mappy.fpm.batches.tomtom.download.json.model.Families.Family;
import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductsDownloaderTest {

    private ProductsDownloader productsDownloader;

    private final HttpClient client = mock(HttpClient.class);

    @Before
    public void setUp() throws Exception {

        HttpResponse productsResponse = mock(HttpResponse.class);
        when(productsResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/tomtom/download/json/products.json").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(productsResponse);

        productsDownloader = new ProductsDownloader(client, "validToken");
    }

    @Test
    public void should_download_product_from_family() {

        Stream<Product> productStream = productsDownloader.apply(new Family("abb", "loc"));

        assertThat(productStream).containsOnly( //
                new Product("EUR", "https://api.test/products/230"), //
                new Product("LAM", "https://api.test/products/530"));
    }
}