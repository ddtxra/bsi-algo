package ch.hcuge.spci.bsi.impl.clabsi.model;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.constants.GermType;
import org.apache.commons.lang3.NotImplementedException;

import java.time.ZonedDateTime;

public abstract class CultureBase implements Culture {

    @Override
    public String getPatientId() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ZonedDateTime getStayBeginDate() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ZonedDateTime getLaboSampleDate() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String getLaboGermName() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean isNosocomial() {
        throw new NotImplementedException("Not implemented");
    }


    @Override
    public GermType getGermType()  {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean isLabGermCommensal()  {
        throw new NotImplementedException("Not implemented");
    }

}
