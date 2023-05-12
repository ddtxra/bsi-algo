package ch.hcuge.spci.bsi.scenarios.model;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.impl.hugv2023.GermType;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCulture implements Culture {

    private static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

    private PositiveHemoCultureHUGv2023 culture;

    private String id;


    public TestCulture(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal) {
        this.id = patientId + "_" +ID_SEQUENCE.incrementAndGet();
        this.culture = new PositiveHemoCultureHUGv2023(patientId, stayBeginDate, laboSampleDate, laboGermName, laboCommensal, new HashMap<>());
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getPatientId() {
        return this.culture.getPatientId();
    }

    @Override
    public String getSampleId() {
        return null;
    }

    @Override
    public String getStayId() {
        return null;
    }

    @Override
    public String getWard() {
        return null;
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

    public GermType getGermType() {
        return this.culture.getGermType();
    }

    @Override
    public Boolean isLabGermCommensal() {
        return this.culture.getGermType().equals(GermType.COMMENSAL);
    }

}