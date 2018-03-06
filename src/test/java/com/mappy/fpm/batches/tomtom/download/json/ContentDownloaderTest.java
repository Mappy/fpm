package com.mappy.fpm.batches.tomtom.download.json;

import com.mappy.fpm.batches.tomtom.download.json.model.Contents.Content;
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

public class ContentDownloaderTest {

    private ContentDownloader contentDownloader;

    private final HttpClient client = mock(HttpClient.class);

    @Before
    public void setUp() throws Exception {

        HttpResponse contentResponse = mock(HttpResponse.class);
        when(contentResponse.getEntity()).thenReturn(new FileEntity(new File(getClass().getResource("/tomtom/download/json/contents.json").toURI())));

        when(client.execute(any(HttpGet.class))).thenReturn(contentResponse);

        contentDownloader = new ContentDownloader(client, "validToken");
    }

    @Test
    public void should_download_all_mn_content_from_release() {

        Stream<Content> content = contentDownloader.apply(new Release("2016.09", "loc1"));

        assertThat(content).containsOnly(
                new Content("eur2016_09-shpd-mn-and-and.7z.001", "https://api.test/contents/792"),
                new Content("eur2016_09-shpd-mn-and-ax.7z.001", "https://api.test/contents/298"),
                new Content("eur2016_09-shpd-mn-fra-ax.7z.001", "https://api.test/contents/352"),
                new Content("eur2016_09-shpd-mn-fra-f20.7z.001", "https://api.test/contents/321"),
                new Content("eur2016_09-shpd-mn-fra-f22.7z.001", "https://api.test/contents/735")
        );
    }
}