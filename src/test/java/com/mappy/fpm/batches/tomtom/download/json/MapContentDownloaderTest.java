package com.mappy.fpm.batches.tomtom.download.json;

import com.mappy.fpm.batches.tomtom.download.json.model.Families.Family;
import com.mappy.fpm.batches.tomtom.download.json.model.Products.Product;
import com.mappy.fpm.batches.tomtom.download.json.model.Releases.Release;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

public class MapContentDownloaderTest {

    private MapContentDownloader mapContentDownloader;

    private final FamiliesDownloader familiesDownloader = mock(FamiliesDownloader.class);
    private final ProductsDownloader productsDownloader = mock(ProductsDownloader.class);
    private final ReleaseDownloader releaseDownloader = mock(ReleaseDownloader.class);
    private final ArchiveDownloader archiveDownloader = mock(ArchiveDownloader.class);

    @Before
    public void setUp(){
        when(familiesDownloader.get()).thenReturn(newArrayList(new Family("abb1", "loc1"), new Family("abb2", "loc2")).stream());

        when(productsDownloader.get(new Family("abb1", "loc1"))).thenReturn(newArrayList(new Product("prod1","loc3"), new Product("prod2", "loc4")).stream());
        when(productsDownloader.get(new Family("abb2", "loc2"))).thenReturn(newArrayList(new Product("prod3","loc5")).stream());

        when(releaseDownloader.get(new Product("prod1","loc3"))).thenReturn(newArrayList(new Release("2016_09", "loc6")).stream());
        when(releaseDownloader.get(new Product("prod2","loc4"))).thenReturn(newArrayList(new Release("2016_09", "loc7")).stream());
        when(releaseDownloader.get(new Product("prod3","loc5"))).thenReturn(newArrayList(new Release("2016_09", "loc8")).stream());

        mapContentDownloader = new MapContentDownloader(familiesDownloader, productsDownloader, releaseDownloader, archiveDownloader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegal_argument_exception_when_call_with_less_than_2_arguments() {
        MapContentDownloader.main(new String[]{"k"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_illegal_argument_exception_when_call_with_more_than_2_arguments() {
        MapContentDownloader.main(new String[]{"k", "h", "l"});
    }

    @Test
    public void should_download_archives(){
        mapContentDownloader.run();

        verify(familiesDownloader).get();
        verify(productsDownloader, times(2)).get(any(Family.class));
        verify(productsDownloader).get(new Family("abb1", "loc1"));
        verify(productsDownloader).get(new Family("abb2", "loc2"));
        verify(releaseDownloader, times(3)).get(any(Product.class));
        verify(releaseDownloader).get(new Product("prod1","loc3"));
        verify(releaseDownloader).get(new Product("prod2","loc4"));
        verify(releaseDownloader).get(new Product("prod3","loc5"));
    }
}