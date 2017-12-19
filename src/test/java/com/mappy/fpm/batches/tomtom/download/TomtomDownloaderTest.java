package com.mappy.fpm.batches.tomtom.download;

import com.google.common.collect.Sets;
import com.mappy.fpm.batches.tomtom.download.MetalinkParser.Metalink;
import com.mappy.fpm.batches.tomtom.download.TomtomCountries.TomtomCountry;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.mappy.fpm.batches.tomtom.download.MetalinkParser.MetalinkUrl.parseMetalinkUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class TomtomDownloaderTest {

    private final MetalinkDownloader downloader = mock(MetalinkDownloader.class);
    private final ShapefileDownloader shapefileDownloader = mock(ShapefileDownloader.class);
    private final TomtomDownloader tomtomDownloader = new TomtomDownloader(downloader, shapefileDownloader,
            Sets.newHashSet(new TomtomCountry("FRA", "France"), new TomtomCountry("GRC", "Grèce")));

    @Test(expected = IllegalStateException.class)
    public void should_throw_an_exception_without_data() throws Exception {
        when(downloader.download()).thenReturn(new Metalink(newArrayList()));

        tomtomDownloader.run();
    }

    @Test
    public void should_throw_an_exception_if_a_country_is_missing() throws Exception {
        when(downloader.download()).thenReturn(new Metalink(newArrayList(
                parseMetalinkUrl("eur2016_09-shpd-mn-fra-ax.7z.001", "http://url1"),
                parseMetalinkUrl("eur2016_09-shpd-mn-fra-f11.7z.001", "http://url2"))));

        try {
            tomtomDownloader.run();
            fail("No exception");
        }
        catch (IllegalStateException e) {
            assertThat(e.getMessage()).contains("No data for Grèce");
        }
    }

    @Test
    public void should_download_shapefiles() throws Exception {
        when(downloader.download()).thenReturn(new Metalink(newArrayList(
                parseMetalinkUrl("eur2016_09-shpd-mn-fra-ax.7z.001", "http://url1"),
                parseMetalinkUrl("eur2016_09-shpd-mn-fra-f11.7z.001", "http://url2"),
                parseMetalinkUrl("lam2016_09-shpd-mn-fra-f11.7z.001", "http://url21"),
                parseMetalinkUrl("eur2016_09-shpd-mn-grc-ax.7z.001", "http://url3"),
                parseMetalinkUrl("mea2016_09-shpd-mn-grc-gr2.7z.001", "http://url4"))));

        tomtomDownloader.run();

        verify(shapefileDownloader).download(new TomtomCountry("FRA", "France"), parseMetalinkUrl("eur2016_09-shpd-mn-fra-ax.7z.001", "http://url1"));
        verify(shapefileDownloader).download(new TomtomCountry("FRA", "France"), parseMetalinkUrl("eur2016_09-shpd-mn-fra-f11.7z.001", "http://url2"));
        verify(shapefileDownloader, never()).download(new TomtomCountry("FRA", "France"), parseMetalinkUrl("lam2016_09-shpd-mn-fra-f11.7z.001", "http://url21"));
        verify(shapefileDownloader).download(new TomtomCountry("GRC", "Grèce"), parseMetalinkUrl("eur2016_09-shpd-mn-grc-ax.7z.001", "http://url3"));
    }
}
