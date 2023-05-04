package ch.hcuge.spci.bsi.impl.praise.model;

import java.time.LocalDate;

/**
 *
 * Denominator (intell in hospital/facility)
 * These are denominator data of all patients in the ward.
 * Not just the patients with positive BC.
 *
 * The information about number of blood cultures is optional:
 *  - If the sender is able to send the negative cultures AND there is no entry in the column “numberOfBloodCultures” in the denominator, then the algorithm will calculated this number using the raw blood cultures (pos AND neg).
 *  - To calculate the number of blood cultures then the ""sampleWardId"" of these BCs (Positive + negative) can't be empty!!
 *  - If the column “numberOfBloodCultures” is provided in the denominator then this number will be used. If the sender sent negative blood cultures as well, the algo will ignore the negative BCs."
 */
public class Denominator {

    /**
     * Optional, because if data are aggregated at a higher level than ward level, wardId looses meaning.
     */
    String wardId;
    Integer patientDays;

    LocalDate startPeriodAggr;

    LocalDate endPeriodAggr;

    /**
     * admissions at aggregation time period
     */
    Integer numberOfAdmissions;

    //FIXME not number of sample ?
    /**
     * Number of BC sets taken (pos + neg)
     * If available, else try to submit all BC's in Bloodculture + sampleWard
     */
    Integer numberOfBloodCultures;

    /**
     * ECDC classification (ward level minimally required)
     * Optional, because if data are aggregated at a higher level than ward level, wardId looses meaning.
     * //FIXME not that true
     */
    String ECDCWardClassification;

}
