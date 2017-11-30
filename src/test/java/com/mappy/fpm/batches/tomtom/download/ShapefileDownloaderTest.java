package com.mappy.fpm.batches.tomtom.download;

import com.mappy.fpm.batches.tomtom.download.MetalinkParser.MetalinkUrl;
import com.mappy.fpm.batches.tomtom.download.TomtomCountries.TomtomCountry;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShapefileDownloaderTest {

    private ShapefileDownloader shapefileDownloader;

    @Before
    public void setUp() throws Exception {
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(getClass().getResourceAsStream("/tomtom/download/and.7z.001"));

        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getEntity()).thenReturn(httpEntity);

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

        shapefileDownloader = new ShapefileDownloader(new File("target/download"), httpClient);
    }

    @Test
    public void should_download_shapefiles() {
        File folder = new File("target/download/Andorre");
        if (folder.exists()) {
            Stream.of(folder.listFiles()).forEach(file -> file.delete());
            folder.delete();
        }
        assertThat(folder.exists()).isFalse();

        TomtomCountry country = new TomtomCountry("AND", "Andorre");
        MetalinkUrl metalinkUrl = MetalinkUrl.parse("eur2016_09-shpd-sp-and-ax.7z.001", "http://url1");

        shapefileDownloader.download(country, metalinkUrl);

        assertThat(folder.listFiles()).hasSize(2);
    }
}