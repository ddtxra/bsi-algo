package ch.hcuge.spci.bsi.impl.praise.model;

import ch.hcuge.spci.BSIUtils;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.exception.BSIException;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EpisodePRAISE implements Episode {

    private List<BloodCulturePRAISE> evidences;
    private List<BloodCulturePRAISE> polyMicrobialEvidences;
    private List<BloodCulturePRAISE> copyStrainEvidences;

    private String patientId;

    private ZonedDateTime stayBeginDate;
    private ZonedDateTime eventDate;

    private Integer birthYear;
    private Integer gender;

    public EpisodePRAISE(BloodCulturePRAISE culture) {

        this.patientId = culture.getPatientId();

        this.evidences = new ArrayList<>();
        this.polyMicrobialEvidences = new ArrayList<>();
        this.copyStrainEvidences = new ArrayList<>();

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
    public Boolean isPolymicrobial() {
        //Seven: Only makes sens to talk about polymicrobials for OBs (HOBs and COBs)
        if(this.isOB()) {
            return this.getDistinctGerms().size() > 1;
        }else {
            return false;
        }
    }

    public String getClassification() {

        var classification = "";
        var subclassification = "";

        if(this.isHOB()) {
            classification ="HOB";

            if(this.containsCSC()){
                subclassification += "HOB-CSC";
            }


        }else {
            classification = "NOT-HOB";

            if(this.isCOB()) {
                subclassification += "COB";

                if(this.containsCSC()){
                    subclassification += "-CSC";
                }

            }
            else if(this.isSolitaryCommensal()) {
                subclassification += "SOLITARY_COMMENSAL";
            }
        }

        if(subclassification.length() > 0){
            return classification + " (" + subclassification + ")";
        }else {
            return classification;
        }
    }

    @Override
    public Set<String> getDistinctGerms() {
        List<String> distinctGerms = this.evidences.stream().map(BloodCulturePRAISE::getLaboGermName).toList();
        var ts = new TreeSet<>(String::compareTo);
        ts.addAll(distinctGerms);
        return ts;
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

    public ZonedDateTime getFirstDateForGerm(String germName) {
        return Objects.requireNonNull(this.evidences.stream().filter(e -> e.getLaboGermName().equals(germName)).min(Comparator.comparing(BloodCulturePRAISE::getLaboSampleDate)).orElse(null)).getLaboSampleDate();
    }

    boolean containsGerm(String germ_name) {
        return this.evidences.stream().anyMatch(e -> e.getLaboGermName().equalsIgnoreCase(germ_name));
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

    public boolean isSolitaryCommensal() {
        return this.evidences.size() == 1 && this.evidences.get(0).isCommensal;
    }

    /** Defines whethere it is an "Onset Bacteremia" **/
    public boolean isOB() {
        return !isSolitaryCommensal();
    }

    public boolean isHOB() {
        return this.isNosocomial() && this.isOB();
    }

    /**
     * community-onset bacteremia (not a HOB, not in scope for PRAISE)
     */
    public boolean isCOB() {
        return !this.isNosocomial() && this.isOB();
    }

    @Override
    public Boolean isNosocomial() {
        return BSIUtils.isNosocomial(this.stayBeginDate, this.eventDate);
    }

    public Boolean containsCSC() {
        return this.evidences.stream().anyMatch(e -> e.isCommensal);
    }

    public void addSecondCSCEvidenceToMakeItAHOB(BloodCulturePRAISE bcp) {

        BloodCulturePRAISE firstEvidence = this.evidences.get(0);

        if(!bcp.isCommensal){
            throw new BSIException("The culture must be a commensal");
        }
        if(!this.isSolitaryCommensal()){
            throw new BSIException("The current 'episode' must be a solitary commensal");
        }
        if(!firstEvidence.getLaboGermName().equals(bcp.getLaboGermName())){
            throw new BSIException("The germs must be the same");
        }

        //if the dates are the same, then the sample id must be different
        if(firstEvidence.getLaboSampleDate().equals(bcp.getLaboSampleDate())){
            if(firstEvidence.getSampleId().equals(bcp.getSampleId())){
                throw new BSIException("The sample id must be different for 2 cultures having the same sampling date");
            }
        }

        this.evidences.add(bcp);
    }

    public void addPolymicrobialEvidences(List<BloodCulturePRAISE> evidences) {
        this.evidences.addAll(evidences);
        this.polyMicrobialEvidences.addAll(evidences);
    }
}
