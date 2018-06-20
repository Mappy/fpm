package com.mappy.fpm.batches.tomtom.download.json.downloader;

import com.mappy.fpm.batches.tomtom.download.json.model.Contents.Content;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectUrlDownloaderTest {

    private DirectUrlDownloader directUrlDownloader;

    @Before
    public void setUp() throws Exception {
        HttpClient client = mock(HttpClient.class);
        HttpResponse directUrlResponse = mock(HttpResponse.class);

        when(directUrlResponse.getEntity()).thenReturn(new StringEntity("{'url': 'directloc'}"));
        when(client.execute(any(HttpGet.class))).thenReturn(directUrlResponse);

        directUrlDownloader = new DirectUrlDownloader(client, "validToken");
    }

    @Test
    public void should_download_archive_from_content() {
        Content content = directUrlDownloader.apply(new Content("content.7z.001", "loc"));

        assertThat(content.getName()).isEqualTo("content.7z.001");
        assertThat(content.getLocation()).isEqualTo("directloc");
    }
}