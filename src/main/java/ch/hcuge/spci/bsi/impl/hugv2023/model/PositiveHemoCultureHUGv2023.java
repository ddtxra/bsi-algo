package ch.hcuge.spci.bsi.impl.hugv2023.model;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.impl.hugv2023.GermType;
import ch.hcuge.spci.bsi.constants.GlobalParameters;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class PositiveHemoCultureHUGv2023 implements Culture {
    protected String patientId;
    protected ZonedDateTime stayBeginDate;
    protected ZonedDateTime laboSampleDate;
    protected String laboGermName;
    protected GermType laboCommensal;
    protected Map<String, String> specificInfo;

    public String getPatientId() {
        return patientId;
    }

    public ZonedDateTime getStayBeginDate() {
        return stayBeginDate;
    }

    public ZonedDateTime getLaboSampleDate() {
        return laboSampleDate;
    }

    public String getLaboGermName() {
        return laboGermName;
    }

    public GermType getGermType() {
        return this.laboCommensal;
    }

    @Override
    public Boolean isLabGermCommensal() {
        return (this.laboCommensal.equals(GermType.COMMENSAL));
    }

    public String getStayBeginFormatted(String format) {
        return DateTimeFormatter.ofPattern(format).format(stayBeginDate);
    }

    public String getLaboCalendarFormatted(String format) {
        return DateTimeFormatter.ofPattern(format).format(laboSampleDate);
    }

    public GermType getLaboCommensal() {
        return laboCommensal;
    }

    public Map<String, String> getSpecificInfo() {
        return specificInfo;
    }

    public PositiveHemoCultureHUGv2023(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal, Map<String, String> specificInfo) {
        this.patientId = patientId;
        this.stayBeginDate = stayBeginDate;
        this.laboSampleDate = laboSampleDate;
        this.laboGermName = laboGermName;
        this.laboCommensal = laboCommensal;
        this.specificInfo = specificInfo;
    }

    public PositiveHemoCultureHUGv2023(String patientId, ZonedDateTime stayBeginDate, ZonedDateTime laboSampleDate, String laboGermName, GermType laboCommensal) {
        this(patientId, stayBeginDate, laboSampleDate, laboGermName, laboCommensal, new HashMap<>());
    }

    private long getNumberOfCalendarDaysSinceAdmission() {
        return ChronoUnit.DAYS.between(this.stayBeginDate.toLocalDate(), this.laboSampleDate.toLocalDate());
    }

    private long getNumberOfDaysSinceAdmission() {
        return ChronoUnit.DAYS.between(this.stayBeginDate, this.laboSampleDate);
    }

    //FIXME should be at the level of the episode
    public boolean isNosocomial() {
        if (GlobalParameters.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL) {
            return getNumberOfCalendarDaysSinceAdmission() >= 2;
        } else {
            return getNumberOfDaysSinceAdmission() >= 2;
        }
    }


}
