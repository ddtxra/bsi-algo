package ch.hcuge.spci.clabsi.algo.impl;
import ch.hcuge.spci.clabsi.algo.BSIClassifier;
import ch.hcuge.spci.clabsi.algo.impl.hugv2023.BSIClassifierHUGv2023;
import ch.hcuge.spci.clabsi.model.Episode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EpisodeIdentifierHUG2023Test {

    @Test
    public void testAlgo(){

        BSIClassifier bsiClassifier = new BSIClassifierHUGv2023();
        List<Episode> episodes = bsiClassifier.processPositiveBloodCultures(Arrays.asList());

        Assert.assertEquals(episodes, null);

    }

}
