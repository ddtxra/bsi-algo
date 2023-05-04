package ch.hcuge.spci.bsi.impl.praise.model;

import ch.hcuge.spci.bsi.Culture;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The sender (participant hospital) can’t deliver the information about the attributable ward for the blood cultures.
 * In this scenario, the sender must send the patients’ movements blocks of all the patients with at least one blood culture.
 * If the variable "admissionDate" of a blood culture is empty, then the algorithm will use the patients' movements blocks to find this data: It is important to send all the stay related movement blocks.
 */
public class BloodCulturePRAISE implements Culture {

    /**
     * A unique identifier of BC micro-organism. Example compound sampleId+isolateNr. Must add isoNr for unique identification purpuses
     */
    public String id;

    /**
     * one sample can have multiple isolates
     */
    public String sampleId;


    public String patientId;


    /** optional if this is incorporated reliably in hospital system //FIXME confusing definition */
    public String episodeOfCareId;


    /** LocalDate **/
    public LocalDate sampleDate;

    public String sampleWardId;

    /**
     * ECDC ward type where culture was taken
     * Optional: Not mandatory, if intelligence is not available; then include movement data for pos cultures
     *
     */
    public String sampleWardECDCWardClassification;


    /**
     * If culture = neg, set to 0.
     * TODO this might not make so much sense for HUG. Not sure how relevant this is for the algorithm.
     */
    public Integer isolateNumber;

    /**
     * The micro organism SNOMED CT code
     * Proposal: setting to missing (0) if pos_neg = neg
     * Optional: MANDATORY if possible
     */
    public String microorgSnomedCTCode;

    /**
     * THEN ALSO IDEALLY INCLUDE SNOMED MAPPING (to identify CSC) -> check feasibility & hierarchy of data; Proposal, setting to missing (0) if pos_neg = neg
     * Optional: MANDATORY only in case of snomed not possible
     */
    public String microorgLocalId;

    /**
     * Classification whether or not the microorganism is a common commensal
     */
    public Boolean isCommensal;

    /**
     * Only if both pos and neg cultures are available 1= pos, 0 = neg
     */
    public Boolean pos_neg;

    /**
     * Ward where pt was 2 days prior to culture taken  if pt was transfered on day of blood culture, take first ward (where patient came from)
     * Optional: Not mandatory, if intelligence is not available; then include movement data for pos cultures
     */
    public String attributableWardId;

    /**
     * Ward where pt was 2 days prior to culture taken  if pt was transfered on day of blood culture, take first ward (where patient came from)
     * Optional: Not mandatory, if intelligence is not available; then include movement data for pos cultures
     */
    public String attributableWardECDCWardClassification;


    /**
     * If the admissionDate can not be calculated by Movement
     */
    public LocalDate admissionDate;

    @Override
    public String getPatientId() {
        return this.patientId;
    }

    @Override
    public ZonedDateTime getStayBeginDate() {
        return this.admissionDate.atStartOfDay(ZoneId.of("UTC"));
    }

    @Override
    public ZonedDateTime getLaboSampleDate() {
        return this.sampleDate.atStartOfDay(ZoneId.of("UTC"));
    }

    @Override
    public String getLaboGermName() {
        if(microorgSnomedCTCode != null){
            return this.microorgSnomedCTCode;
        }

        if(microorgLocalId != null){
            return this.microorgLocalId;
        }

        else return "unknown";
    }

    @Override
    public Boolean isLabGermCommensal() {
        return this.isCommensal;
    }

    @Override
    public boolean isNosocomial() {
        return false;
    }
}
