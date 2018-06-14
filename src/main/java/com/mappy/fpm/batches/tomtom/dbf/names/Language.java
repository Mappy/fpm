package com.mappy.fpm.batches.tomtom.dbf.names;

import lombok.Getter;

import java.util.stream.Stream;

public enum Language {
    ALB("sq"),
    ARA("ar"),
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
    FIN("fi"),
    FRE("fr"),
    FRY("fy"),
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
    RUM("ro"),
    RUL("ru-Latn"),
    RUS("ru"),
    SCC("sr-Latn"),
    SCY("sr"),
    SLO("sk"),
    SLV("sl"),
    SMC("sh"),
    SML("sh-Latn"),
    SPA("es"),
    SWE("sv"),
    TUR("tr"),
    UKL("uk-Latn"),
    UKR("uk"),
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