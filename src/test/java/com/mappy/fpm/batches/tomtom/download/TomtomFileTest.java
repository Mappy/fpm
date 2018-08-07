package com.mappy.fpm.batches.tomtom.download;

import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.*;

public class TomtomFileTest {

    @Test
    public void should_not_have_a_multinet_files_without_dot_and_underscore(){
        Stream<String> filesFilterByUnderscoreAndPoint = TomtomFile.allTomtomFiles("mn")
                .stream()
                .filter(this::notContainsDotsOrUnderscore);

        assertEquals(filesFilterByUnderscoreAndPoint.count(), 0);
    }

    @Test
    public void should_have_one_outerworld_file(){
        Stream<String> filesFilterByUnderscoreAndPoint = TomtomFile.allTomtomFiles("outerworld")
                .stream()
                .filter(this::notContainsDotsOrUnderscore);

        assertEquals(filesFilterByUnderscoreAndPoint.count(), 1);
    }

/*
    @Test
    public void should_have_only_two_file_for_speed_product(){
        assertEquals(TomtomFile.allTomtomFiles("sp").size(), 2);
    }
*/

    private boolean notContainsDotsOrUnderscore(String value) {
        return !value.contains("_") || !value.contains(".");
    }

}
