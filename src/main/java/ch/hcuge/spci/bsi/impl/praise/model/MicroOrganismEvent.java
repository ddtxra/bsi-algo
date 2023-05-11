package ch.hcuge.spci.bsi.impl.praise.model;

import java.time.ZonedDateTime;

/**
 * For Each HOB micro-organism event (line listing)
 * Cave: more than 1 line per HOB possible (if polymicrobial event)
 */
public class MicroOrganismEvent {
    private String HOB_Id;
    private ZonedDateTime eventDate;
    private String patientId;
    private String bloodCulture_id;
    private String microorgSnomedCTCode;
    private String microorgLocalId;

    private Boolean polymicrobial;

    private Boolean CSC_Hob;

    private String wardId;

    private String wardECDCWardClassification;

    private Integer birthYear;
    private String gender;

}
