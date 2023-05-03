package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.impl.hugv2023.BSIClassifierHUGv2023;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PatientCaseServiceTest {

    private boolean compareEpisodes(List<Episode> episodes_expected, List<Episode> episodes_computed) {
        boolean same = true;
        if (episodes_expected.size() != episodes_computed.size()) {
            same = false;

            System.err.println("Size of expected episodes " + episodes_expected.size() + " is not the same as computed episodes " + episodes_computed.size());
            System.err.println("Expected episodes: ");
            episodes_expected.forEach(System.err::println);
            System.err.println("Computed episodes: ");
            episodes_computed.forEach(System.err::println);

        }

        for (var i = 0; i < episodes_expected.size(); i++) {
            if (!compareEpisode(episodes_expected.get(i), episodes_computed.get(i))) {
                same = false;
                System.err.println("Expected: " + episodes_expected.get(i) + " but found " + episodes_computed.get(i));
            }
        }

        return same;
    }

    private boolean compareEpisode(Episode episode1, Episode episode2) {
        return episode1.getPatientId().equals(episode2.getPatientId()) && episode1.getEpisodeDate().equals(episode2.getEpisodeDate()) && episode1.getDistinctGerms().equals(episode2.getDistinctGerms());
    }


    @Test
    public void shouldReadScenariosFromFile() throws IOException, URISyntaxException {

        PatientCaseServiceImpl testCulturesServices = new PatientCaseServiceImpl();
        testCulturesServices.loadContent("cases.tsv");

        var patientIdsSetSize = new HashSet<>(testCulturesServices.getPatientsIds()).size();
        var patientIdsListSize = new HashSet<>(testCulturesServices.getPatientsIds()).size();

        Assert.assertEquals(patientIdsSetSize, patientIdsListSize);
        BSIClassifier classifier = new BSIClassifierHUGv2023();

        AtomicReference<Integer> casesNotTested = new AtomicReference<>(0);

        AtomicBoolean same = new AtomicBoolean(true);
        testCulturesServices.getPatientsIds().forEach(pId -> {
            System.out.println("Testing for " + pId);
            System.out.println("\t" + testCulturesServices.getDescription(pId));

            List<Culture> cultures = testCulturesServices.getCulturesForPatient(pId);
            List<Episode> computedEpisodes = classifier.processCultures(cultures);
            List<Episode> expectedEpisodes = testCulturesServices.getExpectedEpisodesForPatientAndAlgo(pId, "HUG_v2023");

            if (expectedEpisodes.isEmpty()) {
                System.err.println("No expected tests for patient " + pId);
                casesNotTested.getAndSet(casesNotTested.get() + 1);
            } else {
                if (!compareEpisodes(expectedEpisodes, computedEpisodes)) {
                    System.out.println("Comparison failed for patient " + pId);
                    same.set(false);
                } else {
                    System.out.println("Comparison OK");
                }
            }

        });

        if (casesNotTested.get() > 0) {
            System.err.println(casesNotTested.get() + " cases not tested " + ((casesNotTested.get() / (testCulturesServices.getPatientsIds().size() * 1.0))*100) + " %");
        }

        System.out.println(testCulturesServices.getPatientsIds().size() + " cases in total");

        Assert.assertTrue(same.get());

    }

}
