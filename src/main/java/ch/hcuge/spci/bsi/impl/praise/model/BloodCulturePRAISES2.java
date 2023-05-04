package ch.hcuge.spci.bsi.impl.praise.model;

/**
 * The sender (participant hospital) can deliver the information about the attributable ward and its ECDC-Classification for the blood cultures.
 * The information about the patient's admission date must be provided as well
 * In this scenario there is no need to send the patients’ movements blocks
 */
public interface BloodCulturePRAISES2 {

    String getId();
    String getPatientId();
    String getSampleDate();
    String getIsolateNumber();
    Boolean getPosNeg();

}
