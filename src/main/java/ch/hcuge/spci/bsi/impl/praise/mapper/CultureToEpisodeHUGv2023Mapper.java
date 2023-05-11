package ch.hcuge.spci.bsi.impl.praise.mapper;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.impl.hugv2023.GermType;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;
import ch.hcuge.spci.bsi.impl.praise.model.BloodCulturePRAISE;

import java.util.HashMap;

public class CultureToPRAISEEpisodeMapper {

    public static BloodCulturePRAISE mapCulture(Culture culture) {

        return new BloodCulturePRAISE(
                culture.getPatientId(),
                culture.getStayBeginDate(),
                culture.getLaboSampleDate(),
                culture.getLaboGermName(),
                culture.isLabGermCommensal() ? GermType.COMMENSAL : GermType.TRUE_PATHOGEN,
                new HashMap<>());

    }
}
