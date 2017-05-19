package com.mappy.data.batches.tomtom.dbf.names;

import lombok.Getter;

public enum Language {
    ALB("sq"),
    ARA("ar"),
    BAQ("eu"),
    BOS("bs"),
    BUL("bg"),
//    BUN("??"),
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
//    GRE("el"),
    GRL("el"),
    HUN("hu"),
    ICE("is"),
    ITA("it"),
    LAV("lv"),
    LIT("lt"),
    LTZ("lb"),
    MAC("mk"),
//    MAT("??"),
    MLT("mt"),
    NOR("no"),
    POL("pl"),
    POR("pt"),
    ROH("rm"),
    RUL("ru"),
    RUM("ro"),
//    RUS("ru"),
//    SCC("??"),
//    SCR("??"),
//    SCY("??"),
    SLO("sk"),
    SLV("sl"),
//    SMC("??"),
//    SML("??"),
    SPA("es"),
    SWE("sv"),
    TUR("tr"),
    UKL("uk"),
//    UKR("uk"),
//    UND("??"),
//    VAL("??"),
    WEL("cy");


    @Getter
    private final String value;

    Language(String value) {
        this.value = value;
    }

}