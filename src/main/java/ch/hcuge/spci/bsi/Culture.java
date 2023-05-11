package ch.hcuge.spci.bsi;

import java.time.ZonedDateTime;

public interface Culture {

    String getPatientId();

    ZonedDateTime getStayBeginDate();

    ZonedDateTime getLaboSampleDate();

    String getLaboGermName();

    Boolean isLabGermCommensal();

}
