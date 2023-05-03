package ch.hcuge.spci.bsi.scenarios.model;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.exception.BSIException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PatientCase {

    private ArrayList<Culture> cultures;
    private Map<String, List<Episode>> expectedEpisodesForAlgo;
    private String description;
    private String patientId;

    public PatientCase(String description) {
        this.description = description;
        this.cultures = new ArrayList<>();
    }

    public PatientCase() {
        this("");
    }

    public ArrayList<Culture> getCultures() {
        return cultures;
    }


    public String getPatientId(){
        return this.patientId;
    }

    public String getDescription() {
        return description;
    }

    private void initPatientIfNotAlreadyDone(Culture culture) {
        if (Objects.isNull(this.patientId)) {
            this.patientId = culture.getPatientId();
        } else {
            if (!this.patientId.equals(culture.getPatientId())) {
                throw new BSIException("Can't add cultures for another patient " + culture.getPatientId() + " for already existing patient " + this.patientId);
            }
        }
    }

    public int addPositiveHemoculture(Culture culture) {
        initPatientIfNotAlreadyDone(culture);
        this.cultures.add(culture);
        return this.cultures.size();
    }

    public void addToDescription(String comment) {
        this.description += comment;
    }

    public List<Episode> getListOfExpectedEpisodesForAlgo(String algoName) {
        if(expectedEpisodesForAlgo != null && expectedEpisodesForAlgo.containsKey(algoName)){
            return expectedEpisodesForAlgo.get(algoName);
        }else {
            return List.of();
        }
    }

    public void setExpectedEpisodes(Map<String, List<Episode>> expectedEpisodesForAlgo) {
        this.expectedEpisodesForAlgo = expectedEpisodesForAlgo;
    }
}