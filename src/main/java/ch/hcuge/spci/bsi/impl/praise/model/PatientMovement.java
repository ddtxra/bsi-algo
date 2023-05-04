package ch.hcuge.spci.bsi.impl.praise.model;

import java.time.LocalDate;

/**
 * Movement: This data is necessary in the following scenario's.
 * - If data on sampleWardId or attributableWardId is not available for blood culture data, then include movement data for all patients with a positive blood culture - and if not available on an aggregated level - also for the patients with a negative blood culture in order to determine the blood culture intensity
 * - If aggregated denominator data are not available or do not meet the required definitions, movement data for all patients is needed to compute the denominator.
 */
public class PatientMovement {

    public String id;

    public String patientId;

    public String wardId;

    public String wardECDCWardClassification;

    public LocalDate admWardDate;

    /**
     * optional: Patient may still be admitted (hence no discharge date)
     */
    public LocalDate disWardDate;


}
