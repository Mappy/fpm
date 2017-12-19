package com.mappy.fpm.batches.tomtom.download;

import com.mappy.fpm.batches.tomtom.download.MetalinkParser.Metalink;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import java.io.File;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetalinkDownloaderTest {

    private final CloseableHttpClient client = mock(CloseableHttpClient.class);
    private final MetalinkDownloader metalinkDownloader = new MetalinkDownloader("login", "password", "tomtomVersion", "/tmp", client);

    @Test
    public void should_download() throws Exception {
        CloseableHttpResponse tokenResponse = mock(CloseableHttpResponse.class);
        when(tokenResponse.getEntity()).thenReturn(new StringEntity("validToken","UTF-8"));

        CloseableHttpResponse fileResponse = mock(CloseableHttpResponse.class);
        when(fileResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/tomtom/download/test.metalink").toURI())));

        when(client.execute(any(HttpPost.class))).thenReturn(tokenResponse, fileResponse);

        Metalink download = metalinkDownloader.download();

        assertThat(download.getUrls()).isEqualTo(newArrayList(
                new MetalinkParser.MetalinkUrl("test-shpd-mn-and-and.7z.001", "test", "and", "d", "mn", "and", "http://test.com/and.7z.001"),
                new MetalinkParser.MetalinkUrl("test-shpd-mn-and-ax.7z.001", "test", "and", "d", "mn", "ax", "http://test.com/ax.7z.001")
        ));
    }
}