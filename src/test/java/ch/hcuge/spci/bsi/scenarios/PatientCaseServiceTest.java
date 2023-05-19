package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.impl.hugv2023.BSIClassifierHUGv2023;
import ch.hcuge.spci.bsi.impl.praise.BSIClassifierPRAISE;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

        } else {

            System.out.println("Expected episodes: ");
            episodes_expected.forEach(e -> System.out.println("\t" + e));
            System.out.println("Computed episodes: ");
            episodes_computed.forEach(e -> System.out.println("\t" + e));

        }

        for (var i = 0; i < episodes_expected.size(); i++) {
            try {
                if (!compareEpisode(episodes_expected.get(i), episodes_computed.get(i))) {
                    same = false;
                    System.err.println("Expected: " + episodes_expected.get(i) + " but found " + episodes_computed.get(i));
                }
            }catch (IndexOutOfBoundsException ie){
                same = false;
                System.err.println("Failed to compare episodes because they are not the same size");
            }
        }

        if (same) {
            System.out.println("Comparison OK for patient: " + episodes_expected.get(0).getPatientId() + "\n");
        }

        return same;
    }

    private boolean compareEpisode(Episode episode1, Episode episode2) {
        var comparison = episode1.getPatientId().equals(episode2.getPatientId())
                && episode1.getEpisodeDate().toLocalDate().equals(episode2.getEpisodeDate().toLocalDate())
                && episode1.getDistinctGerms().equals(episode2.getDistinctGerms());

        if(!(Objects.isNull(episode1.getClassification())) && !episode1.getClassification().isEmpty()){
            comparison &= episode1.getClassification().equals(episode2.getClassification());
        }
        /* TODO ?
        else if(!(Objects.isNull(episode2.getClassification())) && !episode2.getClassification().isEmpty()){
            comparison &= episode2.getClassification().equals(episode1.getClassification());
        }
         */

        return comparison;
    }


    @Test
    public void shouldTestAllCasesForHUGV2023() throws IOException, URISyntaxException {
        testAlgo("HUG_v2023", "test-scenarios.tsv", new BSIClassifierHUGv2023());
    }

    @Test
    public void shouldTestAllCasesForPRAISE() throws IOException, URISyntaxException {
        testAlgo("PRAISE", "test-scenarios.tsv", new BSIClassifierPRAISE());
    }

    @Test
    public void shouldTestSingleCaseForPRAISE() throws IOException, URISyntaxException {
        testAlgo("PRAISE", "single.tsv", new BSIClassifierPRAISE());
    }


    private void testAlgo(String algo, String fileScenarios, BSIClassifier classifier) throws IOException, URISyntaxException {

        PatientCaseServiceImpl testCulturesServices = new PatientCaseServiceImpl();
        testCulturesServices.loadContent(fileScenarios);

        var failures = new ArrayList<String>();
        var notTested = new ArrayList<String>();

        var patientIdsSetSize = new HashSet<>(testCulturesServices.getPatientsIds()).size();
        var patientIdsListSize = new HashSet<>(testCulturesServices.getPatientsIds()).size();

        Assert.assertEquals(patientIdsSetSize, patientIdsListSize);

        AtomicReference<Integer> casesNotTested = new AtomicReference<>(0);
        AtomicBoolean same = new AtomicBoolean(true);
        testCulturesServices.getPatientsIds().forEach(pId -> {
            System.out.println("Testing for " + pId);
            System.out.println("\t" + testCulturesServices.getDescription(pId));

            List<Episode> computedEpisodes = new ArrayList<Episode>();
            List<Culture> cultures = testCulturesServices.getCulturesForPatient(pId);
            try{
                computedEpisodes = classifier.processCultures(cultures);

            }catch (Exception e){
                same.set(false);
                e.printStackTrace();
            }

            List<Episode> expectedEpisodes = testCulturesServices.getExpectedEpisodesForPatientAndAlgo(pId, algo);

            if (computedEpisodes.isEmpty()) {
                System.err.println("Computed episodes failed, they are empty");
                failures.add(pId);

            } else if (expectedEpisodes.isEmpty()) {
                System.err.println("No expected tests for patient " + pId);
                notTested.add(pId);
                casesNotTested.getAndSet(casesNotTested.get() + 1);
            } else {
                if (!compareEpisodes(expectedEpisodes, computedEpisodes)) {
                    System.out.println("Comparison failed for " + pId);
                    failures.add(pId);
                    same.set(false);
                }
            }

        });

        var casesTested = testCulturesServices.getPatientsIds().size()  - casesNotTested.get();
        System.err.println(casesTested + " cases tested ");

        if (casesNotTested.get() > 0) {
            System.err.println(casesNotTested.get() + " cases not tested: " +  String.join(", ", notTested));
            System.err.println(((casesNotTested.get() / (testCulturesServices.getPatientsIds().size() * 1.0)) * 100) + " % not tested");
        }

        if(failures.size() > 0){
            System.err.println(failures.size() + " failed: " + String.join(", ", failures));
            System.err.println(((failures.size() / ( testCulturesServices.getPatientsIds().size()  * 1.0)) * 100) + " % of failures");
        }

        System.err.println(testCulturesServices.getPatientsIds().size() + " cases in total");

        Assert.assertTrue(same.get());
    }

}
