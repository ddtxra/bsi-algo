package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.impl.praise.BSIClassifierPRAISE;
import ch.hcuge.spci.bsi.impl.praise.model.EpisodePRAISE;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class MDSGenerator {



    private String formatDate(ZonedDateTime zdt){
        return DateTimeFormatter.ofPattern("yyyy/MM/dd").format(zdt);
    }

    private String NN(Object o){
        if(Objects.isNull(o) || o.toString().length() == 0) return "n/a"; else return o.toString();
    }


    @Test
    public void generateCulturesForPraise() throws IOException, URISyntaxException {
        PatientCaseServiceImpl testCulturesServices = new PatientCaseServiceImpl();
        testCulturesServices.loadContent("test-scenarios.tsv");
        BSIClassifier bsiClassifier = new BSIClassifierPRAISE();

        StringBuilder cultureContent = new StringBuilder();
        StringBuilder episodeContentExpected = new StringBuilder();
        StringBuilder hobsOnlyContentExpected = new StringBuilder();
        StringBuilder episodeContentComputed = new StringBuilder();
        StringBuilder hobsOnlyContentComputed = new StringBuilder();

        var headers_for_bc = List.of("secenario#", "id", "sampleId", "patientId", "episodeOfCareId", "sampleDate", "sampleWardId",	"sampleWardECDCWardClassification",	"isolateNumber",	"microorgSnomedCTCode",	"microorgLocalId", "isCSC", "pos_neg",	"attributableWardId",	"attributableWardECDCWardClassification", "admissionDate");
        var headers_for_epi = List.of("patientId", "episodeDate", "microOrganism(s)", "isHOB", "containsCSC", "classification");
        var headers_for_hobsonly = List.of("patientId", "episodeDate", "microOrganism(s)", "containsCSC", "classification");

        cultureContent.append(String.join("\t", headers_for_bc));
        cultureContent.append("\n");

        episodeContentExpected.append(String.join("\t", headers_for_epi));
        episodeContentExpected.append("\n");
        episodeContentComputed.append(String.join("\t", headers_for_epi));
        episodeContentComputed.append("\n");

        hobsOnlyContentExpected.append(String.join("\t", headers_for_hobsonly));
        hobsOnlyContentExpected.append("\n");
        hobsOnlyContentComputed.append(String.join("\t", headers_for_hobsonly));
        hobsOnlyContentComputed.append("\n");



        testCulturesServices.getPatientsIds().forEach(pId -> {

            List<Culture> cultures = testCulturesServices.getCulturesForPatient(pId);
            List<Episode> expectedEpisodes = testCulturesServices.getExpectedEpisodesForPatientAndAlgo(pId, "PRAISE");
            List<Episode> computedEpisodes = bsiClassifier.processCultures(cultures);

            String[] list = {"Ward1", "Ward2", "Ward3"};

            for (Culture culture : cultures) {
                Random r = new Random();
                String ward = list[r.nextInt(list.length)];
                    List<String> row = List.of(
                            pId.replace("patient_", "scenario_"),
                            culture.getId(),
                            NN(culture.getSampleId()),
                            culture.getPatientId(),
                            NN(""),
                            formatDate(culture.getLaboSampleDate()),
                            ward,
                            NN(""),
                            NN(""),
                            NN(""),
                            culture.getLaboGermName(),
                            (culture.isLabGermCommensal() ? "1" : "0"),
                            "1",
                            ward,
                            NN(""),
                            formatDate(culture.getStayBeginDate()));
                    cultureContent.append(String.join("\t", row));
                    cultureContent.append("\n");

            }

            for (Episode episode : expectedEpisodes) {
                List<String> row = List.of(
                        episode.getPatientId(),
                        formatDate(episode.getEpisodeDate()),
                        String.join("+", episode.getDistinctGerms()),
                        (episode.getClassification() != null) ? String.valueOf(!episode.getClassification().contains("NOT-HOB")) : "n/a",
                        episode.getClassification() != null ? String.valueOf(episode.getClassification().contains("CSC")) : "n/a",
                        NN(episode.getClassification())
                        );
                episodeContentExpected.append(String.join("\t", row));
                episodeContentExpected.append("\n");

            }

            for (Episode episode : expectedEpisodes.stream().filter(e -> !e.getClassification().contains("NOT-HOB")).toList()) {
                List<String> row = List.of(
                        episode.getPatientId(),
                        formatDate(episode.getEpisodeDate()),
                        String.join("+", episode.getDistinctGerms()),
                        (episode.getClassification() != null) ? String.valueOf(episode.getClassification().contains("CSC")) : "n/a",
                        NN(episode.getClassification())
                );
                hobsOnlyContentExpected.append(String.join("\t", row));
                hobsOnlyContentExpected.append("\n");

            }

            for (Episode episode : computedEpisodes) {
                List<String> row = List.of(
                        episode.getPatientId(),
                        formatDate(episode.getEpisodeDate()),
                        String.join("+", episode.getDistinctGerms()),
                        (episode.getClassification() != null) ? String.valueOf(!episode.getClassification().contains("NOT-HOB")) : "n/a",
                        episode.getClassification() != null ? String.valueOf(episode.getClassification().contains("CSC")) : "n/a",
                        NN(episode.getClassification())
                );
                episodeContentComputed.append(String.join("\t", row));
                episodeContentComputed.append("\n");

            }

            for (Episode episode : computedEpisodes.stream().filter(e -> !e.getClassification().contains("NOT-HOB")).toList()) {
                List<String> row = List.of(
                        episode.getPatientId(),
                        formatDate(episode.getEpisodeDate()),
                        String.join("+", episode.getDistinctGerms()),
                        String.valueOf(((EpisodePRAISE)episode).containsCSC()),
                        NN(episode.getClassification())
                );
                hobsOnlyContentComputed.append(String.join("\t", row));
                hobsOnlyContentComputed.append("\n");

            }

        });

        Files.writeString(Paths.get("praise-mds/blood-cultures-input.tsv"), cultureContent.toString());
        Files.writeString(Paths.get("praise-mds/hob-output-expected-including-cobs-and-solitary-commensals.tsv"), episodeContentExpected.toString());
        Files.writeString(Paths.get("praise-mds/hob-output-expected-hobs-only.tsv"), hobsOnlyContentExpected.toString());
        Files.writeString(Paths.get("praise-mds/hob-output-computed-including-cobs-and-solitary-commensals.tsv"), episodeContentComputed.toString());
        Files.writeString(Paths.get("praise-mds/hob-output-computed-hobs-only.tsv"), hobsOnlyContentComputed.toString());

    }




}
