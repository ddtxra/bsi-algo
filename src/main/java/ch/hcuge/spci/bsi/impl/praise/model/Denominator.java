package ch.hcuge.spci.bsi.impl.praise.model;

import java.time.LocalDate;

/**
 * The information about number of blood cultures is optional:
 *  - If the sender is able to send the negative cultures AND there is no entry in the column “numberOfBloodCultures” in the denominator, then the algorithm will calculated this number using the raw blood cultures (pos AND neg).
 *  - To calculate the number of blood cultures then the ""sampleWardId"" of these BCs (Positive + negative) can't be empty!!
 *  - If the column “numberOfBloodCultures” is provided in the denominator then this number will be used. If the sender sent negative blood cultures as well, the algo will ignore the negative BCs."
 */
public class Denominator {

    String wardId;
    Integer patientDays;

    LocalDate startPeriodAggr;

    LocalDate endPeriodAggr;

    Integer numberOfAdmissions;

    Integer numberOfBloodCultures;

    String ECDCWardClassification;

}
