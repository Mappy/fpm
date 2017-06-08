package com.mappy.fpm.batches.tomtom.download;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.google.common.collect.Lists;

import java.util.List;

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

    public static List<TomtomCountry> countries() {
        List<TomtomCountry> countries = Lists.newArrayList();
        countries.add(new TomtomCountry("ALB", "Albanie"));
        countries.add(new TomtomCountry("AND", "Andorre"));
        countries.add(new TomtomCountry("AUT", "Autriche"));
        countries.add(new TomtomCountry("BEL", "Belgique"));
        countries.add(new TomtomCountry("BGR", "Bulgarie"));
        countries.add(new TomtomCountry("BIH", "Bosnie-Herzégovine"));
        countries.add(new TomtomCountry("BLR", "Biélorussie"));
        countries.add(new TomtomCountry("CHE", "Suisse"));
        countries.add(new TomtomCountry("CYP", "Chypre"));
        countries.add(new TomtomCountry("CZE", "Tchéquie"));
        countries.add(new TomtomCountry("DEU", "Allemagne"));
        countries.add(new TomtomCountry("DNK", "Danemark"));
        countries.add(new TomtomCountry("ESP", "Espagne"));
        countries.add(new TomtomCountry("EST", "Estonie"));
        countries.add(new TomtomCountry("FIN", "Finlande"));
        countries.add(new TomtomCountry("FRA", "France"));
        countries.add(new TomtomCountry("GBR", "Royaume-Uni"));
        countries.add(new TomtomCountry("GRC", "Grèce"));
        countries.add(new TomtomCountry("HRV", "Croatie"));
        countries.add(new TomtomCountry("HUN", "Hongrie"));
        countries.add(new TomtomCountry("IRL", "Irlande"));
        countries.add(new TomtomCountry("ISL", "Islande"));
        countries.add(new TomtomCountry("ITA", "Italie"));
        countries.add(new TomtomCountry("LTU", "Lituanie"));
        countries.add(new TomtomCountry("LUX", "Luxembourg"));
        countries.add(new TomtomCountry("LVA", "Lettonie"));
        countries.add(new TomtomCountry("MDA", "Moldavie"));
        countries.add(new TomtomCountry("MKD", "Macédoine"));
        countries.add(new TomtomCountry("MLT", "Malte"));
        countries.add(new TomtomCountry("MNE", "Monténégro"));
        countries.add(new TomtomCountry("NLD", "Pays-Bas"));
        countries.add(new TomtomCountry("NOR", "Norvège"));
        countries.add(new TomtomCountry("POL", "Pologne"));
        countries.add(new TomtomCountry("PRT", "Portugal"));
        countries.add(new TomtomCountry("ROU", "Roumanie"));
        countries.add(new TomtomCountry("RUS", "Russie"));
        countries.add(new TomtomCountry("SMR", "Saint-Marin"));
        countries.add(new TomtomCountry("SRB", "Serbie"));
        countries.add(new TomtomCountry("SVK", "Slovaquie"));
        countries.add(new TomtomCountry("SVN", "Slovénie"));
        countries.add(new TomtomCountry("SWE", "Suède"));
        countries.add(new TomtomCountry("TUR", "Turquie"));
        countries.add(new TomtomCountry("UKR", "Ukraine"));
        countries.add(new TomtomCountry("GLP", "Antilles-Françaises"));
        countries.add(new TomtomCountry("GUF", "Guyane-Française"));
        countries.add(new TomtomCountry("REU", "Réunion-et-Mayotte"));
        return countries;
    }

    public static List<TomtomCountry> outerworld() {
        List<TomtomCountry> countries = Lists.newArrayList();
        countries.add(new TomtomCountry("OAT", "Outerworld-Atlantique", true));
        countries.add(new TomtomCountry("OIN", "Outerworld-Indien", true));
        countries.add(new TomtomCountry("OBE", "Outerworld-Belgique", true));
        countries.add(new TomtomCountry("OCP", "Outerworld-Canada-Océan-Pacifique", true));
        countries.add(new TomtomCountry("ODE", "Outerworld-Allemagne", true));
        countries.add(new TomtomCountry("ODK", "Outerworld-Danemark", true));
        countries.add(new TomtomCountry("OES", "Outerworld-Espagne", true));
        countries.add(new TomtomCountry("OFI", "Outerworld-Finlande", true));
        countries.add(new TomtomCountry("OFR", "Outerworld-France", true));
        countries.add(new TomtomCountry("OGB", "Outerworld-Royaume-Uni", true));
        countries.add(new TomtomCountry("OGR", "Outerworld-Grèce", true));
        countries.add(new TomtomCountry("OIE", "Outerworld-Irlande", true));
        countries.add(new TomtomCountry("OIT", "Outerworld-Italie", true));
        countries.add(new TomtomCountry("ONL", "Outerworld-Pays-Bas", true));
        countries.add(new TomtomCountry("ONO", "Outerworld-Norvège", true));
        countries.add(new TomtomCountry("OPL", "Outerworld-Pologne", true));
        countries.add(new TomtomCountry("OPT", "Outerworld-Portugal", true));
        countries.add(new TomtomCountry("ORU", "Outerworld-Russie", true));
        countries.add(new TomtomCountry("OSE", "Outerworld-Suède", true));
        countries.add(new TomtomCountry("OTR", "Outerworld-Turquie", true));
        return countries;
    }
}