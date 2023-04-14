package ch.hcuge.spci.clabsi.algo.impl;
import ch.hcuge.spci.clabsi.algo.EpisodeIdentifier;
import ch.hcuge.spci.clabsi.model.Episode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EpisodeIdentifierHUG2023Test {

    @Test
    public void testAlgo(){

        EpisodeIdentifier episodeIdentifier = new EpisodeIdentifierHUG2023();
        List<Episode> episodes = episodeIdentifier.identifyFromBloodCultures(Arrays.asList());

        Assert.assertEquals(episodes, null);

    }

}
