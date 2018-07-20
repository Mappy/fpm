package com.mappy.fpm.batches.tomtom.dbf.names;

import lombok.Getter;

import java.util.stream.Stream;

public enum Language {
    ALB("sq"),
    ARA("ar"),
    ARM("hy"),
    ARR("hy-Latn"),
    AZE("az"),
    BAQ("eu"),
    BEL("be"),
    BET("be-Latn"),
    BOS("bs"),
    BUL("bg"),
    BUN("bg-Latn"),
    CAT("ca"),
    CZE("cs"),
    DAN("da"),
    DUT("nl"),
    ENG("en"),
    EST("et"),
    FAO("fo"),
    FIN("fi"),
    FRE("fr"),
    FRY("fy"),
    GEL("ka-Latn"),
    GEO("ka"),
    GER("de"),
    GLE("ga"),
    GLG("gl"),
    GRE("el"),
    GRL("el-Latn"),
    HUN("hu"),
    ICE("is"),
    ITA("it"),
    LAV("lv"),
    LIT("lt"),
    LTZ("lb"),
    MAC("mk"),
    MAT("mk-Latn"),
    MLT("mt"),
    NOR("no"),
    POL("pl"),
    POR("pt"),
    ROH("rm"),
    RUL("ru-Latn"),
    RUM("ro"),
    RUS("ru"),
    SCC("sr-Latn"),
    SCR("hr"),
    SCY("sr"),
    SLO("sk"),
    SLV("sl"),
    SMC("cnr"),  // cnr is a custom alpha2 code for Montenegrin (alpha3 code exists: cnr)
    SML("cnr-Latn"),
    SPA("es"),
    SWE("sv"),
    TUR("tr"),
    UKL("uk-Latn"),
    UKR("uk"),
    VAL("oc"),  // unknown language code, interpreted as occitan
    WEL("cy"),
    UND(null);

    @Getter
    private final String value;

    Language(String value) {
        this.value = value;
    }

    public static Language fromValue(String name) {
        return Stream.of(Language.values()).filter(v -> v.name().equals(name)).findFirst().orElse(UND);
    }
}