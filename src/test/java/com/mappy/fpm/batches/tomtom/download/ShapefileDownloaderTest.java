package com.mappy.fpm.batches.tomtom.download;

import com.mappy.fpm.batches.tomtom.download.MetalinkParser.MetalinkUrl;
import com.mappy.fpm.batches.tomtom.download.TomtomCountries.TomtomCountry;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.stream.Stream;

import static com.mappy.fpm.batches.tomtom.download.MetalinkParser.MetalinkUrl.parseMetalinkUrl;
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

        shapefileDownloader = new ShapefileDownloader(new File("target/tests/download"), httpClient);
    }

    @Test
    public void should_download_andorre_shapefiles() {
        File folder = getFile("AND", "Andorre", false);

        assertThat(folder.listFiles()).hasSize(2);
    }

    @Test
    public void should_download_outerworld_shapefiles() {
        File folder = getFile("OAT", "Outerworld-Atlantique", true);

        assertThat(folder.listFiles()).hasSize(1);
    }

    private File getFile(String id, String label, boolean outerworld) {
        File folder = new File("target/tests/download/" + label );
        if (folder.exists()) {
            Stream.of(folder.listFiles()).forEach(File::delete);
            folder.delete();
        }
        assertThat(folder.exists()).isFalse();

        TomtomCountry country = new TomtomCountry(id, label, outerworld);
        MetalinkUrl metalinkUrl = parseMetalinkUrl("eur2016_09-shpd-sp-and-ax.7z.001", "http://url1");

        shapefileDownloader.download(country, metalinkUrl);
        return folder;
    }
}