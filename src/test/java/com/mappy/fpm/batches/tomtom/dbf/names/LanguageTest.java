package com.mappy.fpm.batches.tomtom.dbf.names;

import com.google.common.base.Enums;

import org.junit.Test;

import static java.util.Optional.*;
import static org.junit.Assert.*;

public class LanguageTest {

    @Test
    public void should_tags_tomtom_russian_cyrillic_with_osm_ru() {
        assertEquals(Language.valueOf("RUS").getValue(), "ru");
    }

    @Test
    public void should_tags_tomtom_belarus_cyrillic_with_osm_be() {
        assertEquals(Language.valueOf("BEL").getValue(), "be");
    }

    @Test
    public void should_tags_tomtom_grece_elenic_with_osm_el() {
        assertEquals(Language.valueOf("GRE").getValue(), "el");
    }

    @Test
    public void should_tags_tomtom_macedonian_cyrillic_with_osm_mk() {
        assertEquals(Language.valueOf("MAC").getValue(), "mk");
    }

    @Test
    public void should_tags_tomtom_serbian_cyrillic_with_osm_sr() {
        assertEquals(Language.valueOf("SCY").getValue(), "sr");
    }

    @Test
    public void should_tags_tomtom_serbian_latin_with_osm_srLatn() {
        assertEquals(Language.valueOf("SCC").getValue(), "sr-Latn");
    }

    @Test
    public void should_tags_tomtom_belarussian_latin_with_custom() {
        assertEquals(Language.valueOf("BET").getValue(), "be-Latn");
    }

    @Test
    public void should_tags_tomtom_greek_latin_with_custom() {
        assertEquals(Language.valueOf("GRL").getValue(), "el-Latn");
    }

    @Test
    public void should_tags_tomtom_macedonian_latin_with_custom() {
        assertEquals(Language.valueOf("MAT").getValue(), "mk-Latn");
    }

    @Test
    public void should_tags_tomtom_bulgarian_latin_with_custom() {
        assertEquals(Language.valueOf("BUN").getValue(), "bg-Latn");
    }

    @Test
    public void should_tags_tomtom_russian_latin_with_custom() {
        assertEquals(Language.valueOf("RUL").getValue(), "ru-Latn");
    }

    @Test
    public void should_tags_tomtom_ukrainian_latin_with_custom() {
        assertEquals(Language.valueOf("UKL").getValue(), "uk-Latn");
    }

    @Test
    public void should_tags_tomtom_montenegro_cyrillic_with_osm_sh() {
        assertEquals(Language.valueOf("SMC").getValue(), "sh");
    }

    @Test
    public void should_tags_tomtom_ukrainian_cyrillic_with_osm_uk() {
        assertEquals(Language.valueOf("UKR").getValue(), "uk");
    }

    @Test
    public void should_not_tags_tomtom_latin_should_be_osm_int_name() {
        assertFalse(ofNullable(Enums.getIfPresent(Language.class, "RUT").orNull()).isPresent());
    }

}