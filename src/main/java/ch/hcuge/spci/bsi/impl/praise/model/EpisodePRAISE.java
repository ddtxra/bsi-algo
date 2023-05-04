package ch.hcuge.spci.bsi.impl.praise.model;

import ch.hcuge.spci.bsi.Episode;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;

//FIXME not camel case
public class EpisodePRAISE implements Episode {

    public String HOB_Id; //FIXME should episode id
    public LocalDate eventDate;

    public String patientId;

    public String bloodCulture_id;

    public String microorgSnomedCTCode;

    public Boolean polymicrobial;

    public Boolean CSC_Hob;

    public String wardId;

    public String wardECDCWardClassification;

    public Integer birthYear;

    public PatientGender gender;


    @Override
    public String getPatientId() {
        return this.patientId;
    }

    @Override
    public Boolean isNosocomial() {
        return true; //FIXME ?
    }

    @Override
    public ZonedDateTime getEpisodeDate() {
        return null;
    }

    @Override
    public String getClassification() {
        return null;
    }

    @Override
    public Boolean isPolymicrobial() {
        return null;
    }

    @Override
    public Set<String> getDistinctGerms() {
        return null;
    }
}
