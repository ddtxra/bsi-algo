package ch.hcuge.spci.bsi.impl.praise;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.constants.GlobalParameters;
import ch.hcuge.spci.bsi.impl.hugv2023.mapper.CultureToEpisodeHUGv2023Mapper;
import ch.hcuge.spci.bsi.impl.hugv2023.model.EpisodeHUGv2023;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Based on https://github.com/ddtxra/hob/blob/gh-pages/js/impl/hug-v2023.js
 */
public class BSIClassifierPRAISE implements BSIClassifier {

    @Override
    public List<Episode> processCultures(List<Culture> positiveBloodCultures) {
        if (positiveBloodCultures.size() > 0) {
            return identifiyEpisodes(positiveBloodCultures.stream().map(CultureToEpisodeHUGv2023Mapper::mapCulture).collect(Collectors.toList()));
        }
        return List.of();
    }

    private List<Episode> identifiyEpisodes(List<PositiveHemoCultureHUGv2023> positiveBloodCultures) {
        return List.of();
    }
}


