package com.mappy.fpm.batches.tomtom.download;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class TomtomCountries {
    @Data
    @AllArgsConstructor
    public static class TomtomCountry {
        private final String id;
        private final String label;
        private final boolean outerworld;

        public TomtomCountry(String id, String label) {
            this(id, label, false);
        }
    }

    public static Set<TomtomCountry> countries() {
        Set<TomtomCountry> countries = newHashSet();
        countries.add(new TomtomCountry("ALB", "Albania"));
        countries.add(new TomtomCountry("AND", "Andorra"));
        countries.add(new TomtomCountry("AUT", "Austria"));
        countries.add(new TomtomCountry("BEL", "Belgium"));
        countries.add(new TomtomCountry("BGR", "Bulgaria"));
        countries.add(new TomtomCountry("BIH", "Bosnia-Herzegovina"));
        countries.add(new TomtomCountry("BLR", "Belarus"));
        countries.add(new TomtomCountry("CHE", "Switzerland"));
        countries.add(new TomtomCountry("CYP", "Cyprus"));
        countries.add(new TomtomCountry("CZE", "Czech-Republic"));
        countries.add(new TomtomCountry("DEU", "Germany"));
        countries.add(new TomtomCountry("DNK", "Denmark"));
        countries.add(new TomtomCountry("ESP", "Spain"));
        countries.add(new TomtomCountry("EST", "Estonia"));
        countries.add(new TomtomCountry("FIN", "Finland"));
        countries.add(new TomtomCountry("FRA", "France"));
        countries.add(new TomtomCountry("GBR", "UK"));
        countries.add(new TomtomCountry("GRC", "Greece"));
        countries.add(new TomtomCountry("HRV", "Croatia"));
        countries.add(new TomtomCountry("HUN", "Hungary"));
        countries.add(new TomtomCountry("IRL", "Ireland"));
        countries.add(new TomtomCountry("ISL", "Iceland"));
        countries.add(new TomtomCountry("ITA", "Italy"));
        countries.add(new TomtomCountry("LTU", "Lithuania"));
        countries.add(new TomtomCountry("LUX", "Luxembourg"));
        countries.add(new TomtomCountry("LVA", "Latvia"));
        countries.add(new TomtomCountry("MDA", "Moldova"));
        countries.add(new TomtomCountry("MKD", "Macedonia"));
        countries.add(new TomtomCountry("MLT", "Malta"));
        countries.add(new TomtomCountry("MNE", "Montenegro"));
        countries.add(new TomtomCountry("NLD", "Netherlands"));
        countries.add(new TomtomCountry("NOR", "Norway"));
        countries.add(new TomtomCountry("POL", "Poland"));
        countries.add(new TomtomCountry("PRT", "Portugal"));
        countries.add(new TomtomCountry("ROU", "Romania"));
        countries.add(new TomtomCountry("RUS", "Russia"));
        countries.add(new TomtomCountry("SMR", "San-Marino"));
        countries.add(new TomtomCountry("SRB", "Serbia"));
        countries.add(new TomtomCountry("SVK", "Slovakia"));
        countries.add(new TomtomCountry("SVN", "Slovenia"));
        countries.add(new TomtomCountry("SWE", "Sweden"));
        countries.add(new TomtomCountry("TUR", "Turkey"));
        countries.add(new TomtomCountry("UKR", "Ukraine"));
        countries.add(new TomtomCountry("GLP", "French-West-Indies"));
        countries.add(new TomtomCountry("GUF", "French-Guyana"));
        countries.add(new TomtomCountry("REU", "Reunion-and-Mayotte"));
        return countries;
    }

    public static Set<TomtomCountry> outerworld() {
        Set<TomtomCountry> countries = newHashSet();
        countries.add(new TomtomCountry("OAT", "Outerworld-Atlantic", true));
        countries.add(new TomtomCountry("OIN", "Outerworld-Indian", true));
        countries.add(new TomtomCountry("OBE", "Outerworld-Belgium", true));
        countries.add(new TomtomCountry("OCP", "Outerworld-Canada-Ocean-Pacific", true));
        countries.add(new TomtomCountry("ODE", "Outerworld-Germany", true));
        countries.add(new TomtomCountry("ODK", "Outerworld-Denmark", true));
        countries.add(new TomtomCountry("OES", "Outerworld-Spain", true));
        countries.add(new TomtomCountry("OFI", "Outerworld-Finland", true));
        countries.add(new TomtomCountry("OFR", "Outerworld-France", true));
        countries.add(new TomtomCountry("OGB", "Outerworld-UK", true));
        countries.add(new TomtomCountry("OGR", "Outerworld-Greece", true));
        countries.add(new TomtomCountry("OIE", "Outerworld-Ireland", true));
        countries.add(new TomtomCountry("OIT", "Outerworld-Italy", true));
        countries.add(new TomtomCountry("ONL", "Outerworld-Netherlands", true));
        countries.add(new TomtomCountry("ONO", "Outerworld-Norway", true));
        countries.add(new TomtomCountry("OPL", "Outerworld-Poland", true));
        countries.add(new TomtomCountry("OPT", "Outerworld-Portugal", true));
        countries.add(new TomtomCountry("ORU", "Outerworld-Russia", true));
        countries.add(new TomtomCountry("OSE", "Outerworld-Sweden", true));
        countries.add(new TomtomCountry("OTR", "Outerworld-Turkey", true));
        return countries;
    }
}