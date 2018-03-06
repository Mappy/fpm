package com.mappy.fpm.batches.tomtom.dbf.names;

import lombok.Getter;

public enum Language {
    ALB("sq"),
    ARA("ar"),
    BAQ("eu"),
    BEL("be"),
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
    GRE("el"),
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
    SCC("sr-Latn"),
    SCY("sr"),
    SLO("sk"),
    SLV("sl"),
    SMC("sh"),
    SPA("es"),
    SWE("sv"),
    TUR("tr"),
    UKR("uk"),
    WEL("cy");


    @Getter
    private final String value;

    Language(String value) {
        this.value = value;
    }

}