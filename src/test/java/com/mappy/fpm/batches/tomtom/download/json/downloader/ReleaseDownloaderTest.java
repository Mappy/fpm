package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases.Release;
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

public class ReleaseDownloaderTest {

    private ReleaseDownloader releaseDownloader;

    private final HttpClient client = mock(HttpClient.class);

    @Before
    public void setUp() throws Exception {

        HttpResponse releaseResponse = mock(HttpResponse.class);
        when(releaseResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/tomtom/download/json/releases.json").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(releaseResponse);

        releaseDownloader = new ReleaseDownloader("2016.09", client, "validToken");
    }

    @Test
    public void should_download_only_desired_release_from_product() {

        Stream<Release> release = releaseDownloader.apply(new Product("prod1", "loc1"));

        assertThat(release).containsOnly(new Release("2016.09", "https://api.test/releases/248"));
    }
}