package ch.hcuge.spci.bsi.impl.praise.model;

import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.constants.GermType;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL1;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL2;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL3;
import ch.hcuge.spci.bsi.exception.BSIException;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EpisodePRAISE implements Episode {

    private List<PositiveHemoCultureHUGv2023> evidences;
    private List<PositiveHemoCultureHUGv2023> polyMicrobialEvidences;
    private List<PositiveHemoCultureHUGv2023> copyStrainEvidences;
    private List<PositiveHemoCultureHUGv2023> evidenceBasedOnNonRepeatInterval;

    private String patientId;
    private String laboGermName;
    private GermType laboCommensal;
    private Map<String, String> specificInfo;
    private Boolean polymicrobial;

    private ZonedDateTime stayBeginDate;
    private ZonedDateTime laboSampleDate;


    public EpisodePRAISE(PositiveHemoCultureHUGv2023 culture) {

        this.patientId = culture.getPatientId();

        this.evidences = new ArrayList<>();
        this.polyMicrobialEvidences = new ArrayList<>();
        this.copyStrainEvidences = new ArrayList<>();
        this.evidenceBasedOnNonRepeatInterval = new ArrayList<>(); //FIXME what is this? not the same as copy strain?
        this.polymicrobial = false; // by default it is false

        this.evidences.add(culture);
        this.stayBeginDate = culture.getStayBeginDate();
        this.laboSampleDate = culture.getLaboSampleDate();
        this.laboGermName = culture.getLaboGermName();
        this.laboCommensal = culture.getLaboCommensal();
    }

    //private adds an evidence and ensures the array is sorted
    public void addEvidence(PositiveHemoCultureHUGv2023 culture) {

        if (!culture.getPatientId().equals(this.patientId)) {
            throw new BSIException("Not allowed to add an evidence of another patient" + culture.getPatientId() + " than the original " + this.patientId);
        }

        this.evidences.add(culture);
        this.evidences.sort(Comparator.comparing(PositiveHemoCultureHUGv2023::getLaboSampleDate));

    }

    @Override
    public String getPatientId() {
        return this.patientId;
    }

    @Override
    public Boolean isNosocomial() {
        return this.getFirstEvidence().isNosocomial();
    }


    @Override
    public Boolean isPolymicrobial() {
        return this.polymicrobial;
    }

    public String getClassification() {
        //If only 1 commensal
        if (this.evidences.stream().filter(e -> e.getLaboCommensal().equals(GermType.COMMENSAL)).count() == 1) {
            return "[C]"; //contamination
        }
        if (this.evidences.stream().filter(e -> e.getLaboCommensal().name() != GermType.COMMENSAL.name()).count() > 0 && this.getDistinctGerms().size() > 1) {
            //what if 2 contaminations in same day? is it a polymicrobial or 2 contaminations?
            return "[P]"; //contamination
        }
        return "";
    }

    @Override
    public Set<String> getDistinctGerms() {
        return this.evidences.stream().map(e -> e.getLaboGermName()).collect(Collectors.toSet());
    }

    /**
     * Takes the first episode from the evidences, since they are always sorted from the method addEvidence
     */
    public PositiveHemoCultureHUGv2023 getFirstEvidence() {
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

    public void addPolymicrobialEvidence(PositiveHemoCultureHUGv2023 culture) {
        this.addEvidence(culture);
        this.polymicrobial = true;
        this.polyMicrobialEvidences.add(culture);
    }

    public void addCopyStrainEvidence(PositiveHemoCultureHUGv2023 culture) {
        this.copyStrainEvidences.add(culture);
        this.addEvidence(culture);
    }

    public void addEvidenceBasedOnNonRepeatInterval(PositiveHemoCultureHUGv2023 culture) {
        this.evidenceBasedOnNonRepeatInterval.add(culture);
        this.addEvidence(culture);
    }

    public List<PositiveHemoCultureHUGv2023> getEvidences() {
        return this.evidences;
    }


    @Override
    public BSIClassificationL1 getBSIClassificationL1() {
        return null;
    }

    @Override
    public BSIClassificationL2 getBSIClassificationL2() {
        return null;
    }

    @Override
    public BSIClassificationL3 getBSIClassificationL3() {
        return null;
    }


    public String toString() {
        return Stream.of(this.patientId, this.getFirstEvidence().getLaboSampleDate(), this.getDistinctGerms()).map(Object::toString).collect(Collectors.joining("\t"));
    }

}