package ch.hcuge.spci.bsi.impl.clabsi.model;

import ch.hcuge.spci.bsi.Culture;
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
    public Boolean isLabGermCommensal() {
        throw new NotImplementedException("Not implemented");
    }


    @Override
    public String getId() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String getSampleId() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String getStayId() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String getWard() {
        throw new NotImplementedException("Not implemented");
    }

    abstract protected String getMaterialType();


}
