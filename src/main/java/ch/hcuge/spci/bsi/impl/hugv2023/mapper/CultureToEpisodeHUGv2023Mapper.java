package ch.hcuge.spci.bsi.impl.hugv2023.mapper;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;

import java.util.HashMap;

public class CultureToEpisodeHUGv2023Mapper {

    public static PositiveHemoCultureHUGv2023 mapCulture(Culture culture) {

        return new PositiveHemoCultureHUGv2023(
                culture.getPatientId(),
                culture.getStayBeginDate(),
                culture.getLaboSampleDate(),
                culture.getLaboGermName(),
                culture.getGermType(),
                new HashMap<>());

    }
}
