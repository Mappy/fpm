package com.mappy.fpm.batches.tomtom.download.json;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.junit.Before;

import java.io.File;

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

        releaseDownloader = new ReleaseDownloader(client, "validToken");
    }
}