package com.mappy.fpm.batches.tomtom.dbf.geocodes;

import org.junit.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;

public class InterpolationTest {

    @Test
    public void should_not_tag_tomtom_structure_one() {
        Optional<String> osmValue = Interpolation.getOsmValue(1);
        assertEquals(osmValue, empty());
    }

    @Test
    public void should_tag_tomtom_structure_two_with_interpolation_even() {
        Optional<String> osmValue = Interpolation.getOsmValue(2);
        assertEquals(osmValue, of("even"));
    }

    @Test
    public void should_tag_tomtom_structure_two_with_interpolation_irregular() {
        Optional<String> osmValue = Interpolation.getOsmValue(5);
        assertEquals(osmValue, of("tomtom:irregular"));
    }

}