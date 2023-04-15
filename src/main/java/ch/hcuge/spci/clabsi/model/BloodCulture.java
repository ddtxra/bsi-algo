package ch.hcuge.spci.clabsi.model;

import java.time.ZonedDateTime;
import java.util.Map;

public class BloodCulture extends Culture {

    protected BloodCulture(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal, Map<String, String> specificInfo) {
        super(patientId, stayBeginDate, laboSampleDate, laboGermName, laboCommensal, specificInfo);
    }

    protected BloodCulture(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal) {
        super(patientId, stayBeginDate, laboSampleDate, laboGermName, laboCommensal);
    }
}
