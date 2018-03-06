package com.mappy.fpm.batches.tomtom.download.json;

import com.mappy.fpm.batches.tomtom.download.json.model.Contents.Content;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.FileEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArchiveDownloaderTest {

    private ArchiveDownloader archiveDownloader;

    private final HttpClient client = mock(HttpClient.class);

    @Before
    public void setUp() throws Exception {

        HttpResponse archiveResponse = mock(HttpResponse.class);
        when(archiveResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/tomtom/download/json/test.7z.001").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(archiveResponse);

        archiveDownloader = new ArchiveDownloader(new File("target"), client, "validToken");
    }

    @Test
    public void should_download_archive_from_content() {
        archiveDownloader.download(new Content("content", "loc"));
    }
}