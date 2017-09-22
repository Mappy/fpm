package com.mappy.fpm.batches.tomtom.download;

import com.google.common.collect.Sets;
import com.mappy.fpm.batches.tomtom.download.MetalinkParser.Metalink;
import com.mappy.fpm.batches.tomtom.download.MetalinkParser.MetalinkUrl;
import com.mappy.fpm.batches.tomtom.download.TomtomCountries.TomtomCountry;

import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static org.assertj.core.api.Assertions.*;
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
                MetalinkUrl.parse("eur2016_09-shpd-mn-fra-ax.7z.001", "http://url1"),
                MetalinkUrl.parse("eur2016_09-shpd-mn-fra-f11.7z.001", "http://url2"))));

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
                MetalinkUrl.parse("eur2016_09-shpd-mn-fra-ax.7z.001", "http://url1"),
                MetalinkUrl.parse("eur2016_09-shpd-mn-fra-f11.7z.001", "http://url2"),
                MetalinkUrl.parse("eur2016_09-shpd-mn-grc-ax.7z.001", "http://url3"),
                MetalinkUrl.parse("eur2016_09-shpd-mn-grc-gr1.7z.001", "http://url4"),
                MetalinkUrl.parse("eur2016_09-shpd-mn-grc-gr2.7z.001", "http://url5"))));

        tomtomDownloader.run();

        verify(shapefileDownloader).download(new TomtomCountry("FRA", "France"), MetalinkUrl.parse("eur2016_09-shpd-mn-fra-ax.7z.001", "http://url1"));
        verify(shapefileDownloader).download(new TomtomCountry("FRA", "France"), MetalinkUrl.parse("eur2016_09-shpd-mn-fra-f11.7z.001", "http://url2"));
        verify(shapefileDownloader).download(new TomtomCountry("GRC", "Grèce"), MetalinkUrl.parse("eur2016_09-shpd-mn-grc-ax.7z.001", "http://url3"));
    }
}
