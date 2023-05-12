package ch.hcuge.spci.bsi;

import java.time.ZonedDateTime;

public interface Culture {

    /**
     * Unique lab result identifier
     * @return
     */
    String getId();

    String getPatientId();

    String getSampleId();

    String getStayId();

    String getWard();

    ZonedDateTime getStayBeginDate();

    ZonedDateTime getLaboSampleDate();

    String getLaboGermName();

    Boolean isLabGermCommensal();

}
