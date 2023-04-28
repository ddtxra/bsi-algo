package ch.hcuge.spci.bsi.scenarios.model;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.constants.GermType;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;

import java.time.ZonedDateTime;
import java.util.HashMap;

public class TestCulture implements Culture {

    private PositiveHemoCultureHUGv2023 culture;

    public TestCulture(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal) {
        this.culture = new PositiveHemoCultureHUGv2023(patientId, stayBeginDate, laboSampleDate, laboGermName, laboCommensal, new HashMap<>());
    }

    @Override
    public String getPatientId() {
        return this.culture.getPatientId();
    }

    @Override
    public ZonedDateTime getStayBeginDate() {
        return this.culture.getStayBeginDate();
    }

    @Override
    public ZonedDateTime getLaboSampleDate() {
        return this.culture.getLaboSampleDate();
    }

    @Override
    public String getLaboGermName() {
        return this.culture.getLaboGermName();
    }

    @Override
    public GermType getGermType() {
        return this.culture.getGermType();
    }

    @Override
    public boolean isLabGermCommensal() {
        return this.culture.getGermType().equals(GermType.COMMENSAL);
    }

    @Override
    public boolean isNosocomial() {
        return this.culture.isNosocomial();
    }
}