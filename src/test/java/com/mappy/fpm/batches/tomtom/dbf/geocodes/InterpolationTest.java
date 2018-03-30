package com.mappy.fpm.batches.tomtom.dbf.geocodes;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class InterpolationTest {

    @Test
    public void should_not_tag_tomtom_structure_one() {
        assertThat(Interpolation.getOsmValue(1)).isNull();
    }

    @Test
    public void should_tag_tomtom_structure_two_with_interpolation_even() {
        assertThat(Interpolation.getOsmValue(2)).isEqualTo("even");
    }

    @Test
    public void should_tag_tomtom_structure_two_with_interpolation_irregular() {
        assertThat(Interpolation.getOsmValue(5)).isEqualTo("irregular:tomtom");
    }

}