package com.mappy.fpm.batches.tomtom.helpers;

import lombok.Getter;

import static java.util.stream.Stream.of;

public class OsmLevelGenerator {

    private enum AdminLevel {
        A0("2"), A1("4"), A2("6"), A6("7"), A7("6"), A8("8"), A9("9"), A10("10");

        @Getter
        private final String osmLevel;

        AdminLevel(String osmLevel) {
            this.osmLevel = osmLevel;
        }
    }

    protected enum CountryLevel {

        BEL(7, "7"), DEU(9, "10"), GBR(9, "10"), RUS(7, "7");

        private final Integer tomtomLevel;
        private final String osmLevel;

        CountryLevel(Integer tomtomLevel, String osmLevel) {
            this.tomtomLevel = tomtomLevel;
            this.osmLevel = osmLevel;
        }

        public String getOsmLevel(Integer tomtomLevel, String defaultOsmLevel) {
            return this.tomtomLevel.equals(tomtomLevel) ? osmLevel : defaultOsmLevel;
        }
    }

    public String getOsmLevel(String zone, Integer tomtomLevel) {

        String defaultOsmLevel = AdminLevel.valueOf("A" + tomtomLevel).getOsmLevel();

        if(zone != null && zone.length() > 2) {
            String key = zone.toUpperCase().substring(0, 3);

            return of(CountryLevel.values()).map(Enum::name).anyMatch(name -> name.equals(key)) //
                    ? CountryLevel.valueOf(key).getOsmLevel(tomtomLevel, defaultOsmLevel) //
                    : defaultOsmLevel;
        }
        return defaultOsmLevel;
    }
}
