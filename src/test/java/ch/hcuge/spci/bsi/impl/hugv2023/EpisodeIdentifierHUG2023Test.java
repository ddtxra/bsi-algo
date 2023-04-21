package ch.hcuge.spci.bsi.impl.hugv2023;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Episode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EpisodeIdentifierHUG2023Test {

    @Test
    public void testAlgo() {

        BSIClassifier bsiClassifier = new BSIClassifierHUGv2023();
        List<Episode> episodes = bsiClassifier.processCultures(Arrays.asList());

        Assert.assertEquals(episodes, List.of());

    }

}
