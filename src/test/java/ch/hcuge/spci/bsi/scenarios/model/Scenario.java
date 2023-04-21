package ch.hcuge.spci.bsi.scenarios.model;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scenario {

    private ArrayList<Culture> cultures;
    private Map<String, List<Episode>> expectedEpisodesForAlgo;
    private String description;

    public Scenario(String description) {
        this.description = description;
        this.cultures = new ArrayList<>();
    }

    public Scenario() {
        this("");
    }

    public ArrayList<Culture> getCultures() {
        return cultures;
    }

    public String getDescription() {
        return description;
    }

    public int addPositiveHemoculture(Culture culture) {
        this.cultures.add(culture);
        return this.cultures.size();
    }


    public void addToDescription(String comment) {
        this.description += comment;
    }

    public List<Episode> getListOfExpectedEpisodesForAlgo(String algoName) {
        return expectedEpisodesForAlgo.get(algoName);
    }


    public void setExpectedEpisodes(Map<String, List<Episode>> expectedEpisodesForAlgo) {
        this.expectedEpisodesForAlgo = expectedEpisodesForAlgo;
    }
}