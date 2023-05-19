package ch.hcuge.spci.bsi.scenarios.model;

import ch.hcuge.spci.BSIUtils;
import ch.hcuge.spci.bsi.Episode;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EpisodeImplForTest implements Episode {

    private String patientId;
    private String episodeDate;
    private String germs;

    public String getTestAlgoName() {
        return testAlgoName;
    }

    private String testAlgoName;

    public void setClassification(String classification) {
        this.classification = classification;
    }

    private String classification;

    public EpisodeImplForTest(String testAlgoName, String patientId, String episodeDate, String germs) {
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
        return this.classification.contains("HOB");
    }

    @Override
    public ZonedDateTime getEpisodeDate() {
        return ZonedDateTime.parse(this.episodeDate + "T00:00:00.000Z");
    }

    @Override
    public String getClassification() {
        return this.classification;
    }

    public String toString() {
        var elements = new ArrayList<String>();
        elements.add(this.patientId);
        elements.add(this.getEpisodeDate().toLocalDate().toString());
        elements.add(this.getDistinctGerms().toString());

        if((!Objects.isNull(this.classification)) && (this.classification.length() > 0)) {
            elements.add(this.getClassification());
        }

        return String.join("\t", elements);
    }
}
