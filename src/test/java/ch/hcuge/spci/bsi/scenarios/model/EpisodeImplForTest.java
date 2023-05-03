package ch.hcuge.spci.bsi.scenarios.model;

import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL1;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL2;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL3;

import java.time.ZonedDateTime;
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
        return null;
    }

    @Override
    public ZonedDateTime getEpisodeDate() {
        return ZonedDateTime.parse(this.episodeDate + "T00:00:00.000Z");
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
        return Stream.of(this.patientId, this.getEpisodeDate(), this.getDistinctGerms()).map(Object::toString).collect(Collectors.joining("\t"));
    }
}
