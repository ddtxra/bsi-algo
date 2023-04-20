package ch.hcuge.spci.clabsi;

import ch.hcuge.spci.clabsi.model.Episode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scenario {
    private String description;
    private ArrayList<String> positive_hemocultures;
    private Map<String, List<Episode>> episodes_computed;
    private Map<String, List<Episode>> episodes_expected;

    public Scenario() {
        this.description = "";
        this.positive_hemocultures = new ArrayList<>();
        this.episodes_computed = new HashMap<>();
        this.episodes_expected = new HashMap<>();
    }

    public int addPositiveHemoculture(String positive_hemoculture) {
        this.positive_hemocultures.add(positive_hemoculture);
        return this.positive_hemocultures.size();
    }

    public void addDescription(String description) {
        this.description += description + "<br>";
    }

    public void addEpisodeComputation(String algo_name, List<Episode> episodes) {
        this.episodes_computed.put(algo_name, episodes);
    }

    public void setEpisodesExpectedByAlgo(Map<String, List<Episode>> episodes_by_algo) {
        this.episodes_expected = episodes_by_algo;
    }
}