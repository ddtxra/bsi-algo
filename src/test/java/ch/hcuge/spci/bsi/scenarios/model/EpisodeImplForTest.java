package ch.hcuge.spci.bsi.scenarios.model;

import ch.hcuge.spci.bsi.Episode;
import java.util.Set;

public class EpisodeImplForTest implements Episode {

    private String patientId;
    private String episodeDate;
    private String germs;

    public String getTestAlgoName() {
        return testAlgoName;
    }

    private String testAlgoName;

    public EpisodeImplForTest(String testAlgoName, String patientId, String episodeDate, String germs){
        this.testAlgoName = testAlgoName;
        this.patientId = patientId;
        this.episodeDate = episodeDate;
        this.germs = germs;
    }

    @Override
    public String getPatientId() {
        return this.patientId;
    }

    @Override
    public Boolean isPolymicrobial() {
        return germs.contains("+");
    }

    @Override
    public Set<String> getDistinctGerms() {
        return Set.of(germs.split("\\+"));
    }

    // CLASSIFICATION
    @Override
    public Boolean isNosocomial() {
        return null;
    }

    @Override
    public String getClassification() {
        return null;
    }
}
