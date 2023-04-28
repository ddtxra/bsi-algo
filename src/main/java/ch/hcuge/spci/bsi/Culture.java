package ch.hcuge.spci.bsi;

import ch.hcuge.spci.bsi.constants.GermType;

import java.time.ZonedDateTime;

public interface Culture {

    String getPatientId();

    ZonedDateTime getStayBeginDate();

    ZonedDateTime getLaboSampleDate();

    String getLaboGermName();

    //shouldn't be here
    @Deprecated
    GermType getGermType(); //FIXME necessary here?

    boolean isLabGermCommensal();

    boolean isNosocomial();
}
