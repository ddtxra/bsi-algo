package ch.hcuge.spci.bsi.impl.praise.model;

import ch.hcuge.spci.BSIUtils;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.exception.BSIException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EpisodePRAISE implements Episode {

    private List<BloodCulturePRAISE> evidences;
    private List<BloodCulturePRAISE> polyMicrobialEvidences;
    private List<BloodCulturePRAISE> copyStrainEvidences;

    private String patientId;
    private Boolean laboCommensal;
    private Boolean polymicrobial;

    private ZonedDateTime stayBeginDate;
    private ZonedDateTime eventDate;

    private Integer birthYear;

    private Integer gender;

    public EpisodePRAISE(BloodCulturePRAISE culture) {

        this.patientId = culture.getPatientId();

        this.evidences = new ArrayList<>();
        this.polyMicrobialEvidences = new ArrayList<>();
        this.copyStrainEvidences = new ArrayList<>();
        this.polymicrobial = false; // by default it is false

        this.evidences.add(culture);
        this.stayBeginDate = culture.getStayBeginDate();
        this.eventDate = culture.getLaboSampleDate();
    }

    //private adds an evidence and ensures the array is sorted
    public void addEvidence(BloodCulturePRAISE culture) {

        if (!culture.getPatientId().equals(this.patientId)) {
            throw new BSIException("Not allowed to add an evidence of another patient" + culture.getPatientId() + " than the original " + this.patientId);
        }

        this.evidences.add(culture);
        this.evidences.sort(Comparator.comparing(BloodCulturePRAISE::getLaboSampleDate));

    }

    @Override
    public String getPatientId() {
        return this.patientId;
    }

    @Override
    public Boolean isNosocomial() {
        return BSIUtils.isNosocomial(this.stayBeginDate, this.eventDate);
    }


    @Override
    public Boolean isPolymicrobial() {
        return this.polymicrobial;
    }

    public String getClassification() {

        if (!this.isNosocomial()) {
            return "NOT-HOB";
        } else {
            //If there is at least one BC that is not a commensal (true pathogen), then it is a HOB
            if (this.evidences.stream().anyMatch(e -> !e.isCommensal)) {
                return "HOB";
            } else { //no true pathogenes = only commensals
                //If there is only one commensal, then it is a contamination
                if (this.evidences.size() == 1) {
                    return "CONTAMINATION";
                } else {
                    long smallestTimeWindow = Long.MAX_VALUE;
                    for (var i = 0; i < this.evidences.size(); i++) {
                        for (var j = 0; j < this.evidences.size(); j++) {
                            var evi1 = this.evidences.get(i);
                            var evi2 = this.evidences.get(j);
                            //FIXME for different samples? (this is not defined on the specs)?
                            if (!Objects.equals(evi1.getSampleId(), evi2.getSampleId())) {
                                var day_between_cultures = ChronoUnit.DAYS.between(evi1.getLaboSampleDate(), evi2.getLaboSampleDate());
                                smallestTimeWindow = Math.min(smallestTimeWindow, day_between_cultures);
                            }
                        }
                    }
                    if (smallestTimeWindow < 3) {
                        return "HOB";
                    } else {
                        return "CONTAMINATION";
                    }
                }


            }
        }
    }

    @Override
    public Set<String> getDistinctGerms() {
        return this.evidences.stream().map(BloodCulturePRAISE::getLaboGermName).collect(Collectors.toSet());
    }

    /**
     * Takes the first episode from the evidences, since they are always sorted from the method addEvidence
     */
    public BloodCulturePRAISE getFirstEvidence() {
        return this.evidences.get(0);
    }

    /**
     * Takes the first episode from the evidences
     */
    public ZonedDateTime getEpisodeDate() {
        return this.getFirstEvidence().getLaboSampleDate();
    }

    boolean containsGerm(String germ_name) {
        return this.evidences.stream().anyMatch(e -> e.getLaboGermName().equalsIgnoreCase(germ_name));
    }

    public void addPolymicrobialEvidence(BloodCulturePRAISE culture) {
        this.addEvidence(culture);
        this.polymicrobial = true;
        this.polyMicrobialEvidences.add(culture);
    }

    public void addCopyStrainEvidence(BloodCulturePRAISE culture) {
        this.copyStrainEvidences.add(culture);
        this.addEvidence(culture);
    }


    public List<BloodCulturePRAISE> getEvidences() {
        return this.evidences;
    }

    public String toString() {
        return Stream.of(this.patientId, this.getEpisodeDate().toLocalDate(), this.getDistinctGerms(), this.getClassification()).map(Object::toString).collect(Collectors.joining("\t"));
    }

}
