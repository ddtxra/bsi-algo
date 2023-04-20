package ch.hcuge.spci.clabsi.model;

import ch.hcuge.spci.clabsi.model.Culture;
import ch.hcuge.spci.clabsi.model.GermType;

import java.time.ZonedDateTime;
import java.util.Map;

public class BloodCulture extends Culture {

    protected BloodCulture(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal, Map<String, String> specificInfo) {
        super(patientId, stayBeginDate, laboSampleDate, laboGermName, laboCommensal, specificInfo);
    }

    public BloodCulture(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal) {
        super(patientId, stayBeginDate, laboSampleDate, laboGermName, laboCommensal);
    }
}
