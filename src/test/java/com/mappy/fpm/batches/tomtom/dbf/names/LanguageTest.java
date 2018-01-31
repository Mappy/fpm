package com.mappy.fpm.batches.tomtom.dbf.names;

import org.junit.Test;

import static org.junit.Assert.*;

public class LanguageTest {

    @Test
    public void should_tags_tomtom_russian_cyrillic_with_russian() {
        assertEquals(Language.valueOf("RUS").getValue(), "ru");
    }

}