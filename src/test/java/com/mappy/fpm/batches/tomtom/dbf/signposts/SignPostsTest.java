package com.mappy.fpm.batches.tomtom.dbf.signposts;

import com.mappy.fpm.batches.tomtom.TomtomFolder;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SignPostsTest {
    private final TomtomFolder folder = mock(TomtomFolder.class);

    @Before
    public void setup() {
        when(folder.getFile("sg.dbf")).thenReturn(getClass().getResource("/tomtom/sg.dbf").getPath());
        when(folder.getFile("nw.dbf")).thenReturn(getClass().getResource("/tomtom/signposts/sign_on_3_ways_nw.dbf").getPath());
    }

    @Test
    public void should_load_signs() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.signPostContentFor(12500001485141L)).isEmpty();
        assertThat(signPosts.signPostContentFor(12500001658893L)).containsExactly("A10", "Bordeaux", "Nantes", "Quai d'Issy");
        assertThat(signPosts.signPostColourFor(12500001658893L)).containsExactly("red", "red", "red", "white");
    }

    @Test
    public void should_generate_tags_for_one_way() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.getTags(12500001485141L, true, 0L, 0L)).isEmpty();
        assertThat(signPosts.getTags(12500001658893L, true, 0L, 0L))
                .contains(entry("destination", "A10;Bordeaux;Nantes;Quai d'Issy"), entry("destination:colour", "red;red;red;white"));
    }

    @Test
    public void should_generate_tags_for_bidirectional_way() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.getTags(12500001658893L, false, 0L, 12500002912860L)).contains(entry("destination:forward", "A10;Bordeaux;Nantes;Quai d'Issy"));
        assertThat(signPosts.getTags(12500001658893L, false, 12500002912860L, 12500002912861L))
                .contains(entry("destination:backward", "A10;Bordeaux;Nantes;Quai d'Issy"), entry("destination:backward:colour", "red;red;red;white"));
    }

    @Test
    public void should_generate_tag_for_signpost_on_intermediate_junction() {
        when(folder.getFile("sg.dbf")).thenReturn(getClass().getResource("/tomtom/signposts/sign_on_3_ways_sg.dbf").getPath());
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/signposts/sign_on_3_ways_si.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/signposts/sign_on_3_ways_sp.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        Map<String, String> tags = signPosts.getTags(14700000013542L, false, 14700000005231L, 14700000005138L);
        assertThat(tags).isEmpty();

        tags = signPosts.getTags(14700000023315L, false, 14700000005231L, 14700000025680L);
        assertThat(tags).isEmpty();

        tags = signPosts.getTags(14700000021236L, false, 14700000025680L, 14700000025681L);
        assertThat(tags).contains(entry("destination:forward", "Sliema;Gzira"), entry("destination:forward:colour", "white;yellow"));
    }

    @Test
    public void should_prefer_large_sign() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si2.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp2.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.signPostContentFor(12500001191560L)).containsExactly("Fontainebleau", "Lyon", "Milly la Forét", "Saint-Fargeau-Ponthierry");
    }

    @Test
    public void should_load_pictogramme() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si3.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp3.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.exitRefFor(10560001774691L)).contains("17");
        assertThat(signPosts.symbolRefFor(10560001774691L)).containsExactly("none", "none", "none", "industrial");
        assertThat(signPosts.signPostContentFor(10560001774691L)).containsExactly("Brussel", "Bruxelles-Centre", "Brussel-Centrum", "Anderlecht");
    }

    @Test
    public void should_distinguish_road_references_and_destinations_references() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si4.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp4.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.signPostHeaderFor(10560001579633L)).containsExactly("E19");
        assertThat(signPosts.signPostContentFor(10560001579633L)).containsExactly("Bruxelles", "Liège", "Mons", "E42");
        assertThat(signPosts.signPostColourFor(10560001579633L)).containsExactly("white", "white", "white", "green", "green");
    }

    @Test
    public void should_load_road_references_on_shield() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si5.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp5.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.signPostHeaderFor(12500058400359L)).containsExactly("A2", "E19");
        assertThat(signPosts.signPostContentFor(12500058400359L)).containsExactly("Cambrai", "Valenciennes", "Bruxelles", "Liège");
    }

    @Test
    public void should_load_signpost_content_with_picto() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si6.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp6.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.symbolRefFor(12500001165986L)).containsExactly("none", "airport", "none", "none", "none", "none");
        assertThat(signPosts.signPostContentFor(12500001165986L)).containsExactly("Sarcelles", "Charles de Gaulle", "Lille", "Saint-Denis-Universités", "Pierrefitte-sur-Seine", "Stains");
    }

    @Test
    public void should_get_numeric_value_for_exit_ref_and_label_for_destination() {
        when(folder.getFile("si.dbf")).thenReturn(getClass().getResource("/tomtom/si7.dbf").getPath());
        when(folder.getFile("sp.dbf")).thenReturn(getClass().getResource("/tomtom/sp7.dbf").getPath());

        SignPosts signPosts = new SignPosts(folder);

        assertThat(signPosts.exitRefFor(10560001774691L)).contains("2");
        assertThat(signPosts.signPostContentFor(10560001774691L)).containsExactlyInAnyOrder("Villejuif", "Arcueil", "Le Kremlin-Bicêtre");

        assertThat(signPosts.exitRefFor(20560001774200L)).contains("10.1");
        assertThat(signPosts.signPostContentFor(20560001774200L)).containsExactlyInAnyOrder("Eurocentre", "Grenade");

        assertThat(signPosts.exitRefFor(30560001774400L)).contains("4a");
        assertThat(signPosts.signPostContentFor(30560001774400L)).containsExactlyInAnyOrder("Garges-lès-Gonesse", "Stains");
    }
}
