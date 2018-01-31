package com.mappy.fpm.batches.tomtom.dbf.names;

import lombok.Getter;

public enum Language {
    ALB("sq"),
    ARA("ar"),
    BAQ("eu"),
    BOS("bs"),
    BUL("bg"),
    CAT("ca"),
    CZE("cs"),
    DAN("da"),
    DUT("nl"),
    ENG("en"),
    EST("et"),
    FIN("fi"),
    FRE("fr"),
    FRY("fy"),
    GER("de"),
    GLE("ga"),
    GLG("gl"),
    GRL("el"),
    HUN("hu"),
    ICE("is"),
    ITA("it"),
    LAV("lv"),
    LIT("lt"),
    LTZ("lb"),
    MAC("mk"),
    MLT("mt"),
    NOR("no"),
    POL("pl"),
    POR("pt"),
    ROH("rm"),
    RUM("ro"),
    RUS("ru"),
    SLO("sk"),
    SLV("sl"),
    SPA("es"),
    SWE("sv"),
    TUR("tr"),
    UKL("uk"),
    WEL("cy");


    @Getter
    private final String value;

    Language(String value) {
        this.value = value;
    }

}