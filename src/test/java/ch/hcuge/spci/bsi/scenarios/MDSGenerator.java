package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.impl.praise.BSIClassifierPRAISE;
import ch.hcuge.spci.bsi.impl.praise.model.EpisodePRAISE;
import ch.hcuge.spci.bsi.scenarios.model.EpisodeImplForTest;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MDSGenerator {




    @Test
    public void generateCulturesForPraise() throws IOException, URISyntaxException {
        var SEPARATOR = ";";

        PatientCaseServiceImpl testCulturesServices = new PatientCaseServiceImpl();
        testCulturesServices.loadContent("test-scenarios.tsv");
        BSIClassifier bsiClassifier = new BSIClassifierPRAISE();

        StringBuilder patientContent = new StringBuilder();
        StringBuilder cultureContent = new StringBuilder();

        StringBuilder episodeContentExpected = new StringBuilder();
        StringBuilder hobsOnlyContentExpected = new StringBuilder();
        StringBuilder episodeContentComputed = new StringBuilder();
        StringBuilder hobsOnlyContentComputed = new StringBuilder();

        var headers_for_patient = List.of("patientId", "gender", "birthDate", "deathDate", "inHospitalMortality");
        var headers_for_bc = List.of("bloodCultureid", "sampleId", "patientId", "episodeOfCareId", "sampleDate", "sampleWardId",	"sampleWardECDCWardClassification",	"isolateNumber",	"microorgSnomedCTCode",	"microorgLocalId", "isCSC", "pos_neg",	"attributableWardId",	"attrWardECDCWardClassification", "admHospDate");
        var headers_for_episodes = List.of("patientId", "episodeDate", "microOrganism(s)", "isHOB", "isPolymicrobial", "containsCSC", "classification");

        patientContent.append(String.join(SEPARATOR, headers_for_patient));
        patientContent.append("\n");

        cultureContent.append(String.join(SEPARATOR, headers_for_bc));
        cultureContent.append("\n");

        episodeContentExpected.append(String.join(SEPARATOR, headers_for_episodes));
        episodeContentExpected.append("\n");
        episodeContentComputed.append(String.join(SEPARATOR, headers_for_episodes));
        episodeContentComputed.append("\n");

        hobsOnlyContentExpected.append(String.join(SEPARATOR, headers_for_episodes));
        hobsOnlyContentExpected.append("\n");
        hobsOnlyContentComputed.append(String.join(SEPARATOR, headers_for_episodes));
        hobsOnlyContentComputed.append("\n");

        AtomicInteger cnt = new AtomicInteger(0);


        testCulturesServices.getPatientsIds().forEach(pId -> {

            var seed = cnt.incrementAndGet();
            Random random = new Random(seed);

            String gender = random.nextDouble() < 1.0 / 20 ? "U" : (random.nextDouble() < 0.5 ? "M" : "F");
            String birthDate = java.time.LocalDate.of(1930, 1, 1).plusDays(random.nextInt(27371)).format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String deathDate = birthDate.isEmpty() || random.nextDouble() > 0.1 ? "" : LocalDate.of(2021, 2, 1).plusDays(random.nextInt(731)).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String inHospitalMortality = "";
            if(!deathDate.equals("")){
                inHospitalMortality = (random.nextDouble() < 0.7 ? "1" : "0");
            }

            List<String> patientRow = List.of(pId, gender, birthDate, deathDate, inHospitalMortality);
            patientContent.append(String.join(SEPARATOR, patientRow));
            patientContent.append("\n");

            List<Culture> cultures = testCulturesServices.getCulturesForPatient(pId);
            List<Episode> expectedEpisodes = testCulturesServices.getExpectedEpisodesForPatientAndAlgo(pId, "PRAISE");
            List<Episode> computedEpisodes = bsiClassifier.processCultures(cultures);

            String[] list = {"Ward1", "Ward2", "Ward3"};

            for (Culture culture : cultures) {
                String ward = list[random.nextInt(list.length)];
                    List<String> row = List.of(
                            culture.getId(),
                            culture.getSampleId(),
                            culture.getPatientId(),
                            "",
                            formatDate(culture.getLaboSampleDate()),
                            ward,
                            "",
                            "",
                            "",
                            culture.getLaboGermName(),
                            (culture.isLabGermCommensal() ? "1" : "0"),
                            "1",
                            ward,
                            "",
                            formatDate(culture.getStayBeginDate()));
                    cultureContent.append(String.join(SEPARATOR, row));
                    cultureContent.append("\n");

            }


            //EXPECTED
            for (Episode episode : expectedEpisodes) {
                episodeContentExpected.append(String.join(SEPARATOR, getRow((EpisodeImplForTest) episode)));
                episodeContentExpected.append("\n");

                if(!episode.getClassification().contains("NOT-HOB")){
                    hobsOnlyContentExpected.append(String.join(SEPARATOR, getRow((EpisodeImplForTest)episode)));
                    hobsOnlyContentExpected.append("\n");
                }

            }

            //COMPUTED
            for (Episode episode : computedEpisodes) {
                episodeContentComputed.append(String.join(SEPARATOR, getRow((EpisodePRAISE)episode)));
                episodeContentComputed.append("\n");

                if(!episode.getClassification().contains("NOT-HOB")) {
                    hobsOnlyContentComputed.append(String.join(SEPARATOR, getRow((EpisodePRAISE)episode)));
                    hobsOnlyContentComputed.append("\n");
                }

            }

        });

        Files.writeString(Paths.get("praise-mds/PATIENTS.CSV"), patientContent.toString(),  StandardCharsets.UTF_8);
        Files.writeString(Paths.get("praise-mds/BLOODCULTURES.CSV"), cultureContent.toString(),  StandardCharsets.UTF_8);

        Files.writeString(Paths.get("praise-mds/hob-output-expected-including-cobs-and-solitary-commensals.csv"), episodeContentExpected.toString(),  StandardCharsets.UTF_8);
        Files.writeString(Paths.get("praise-mds/hob-output-expected-hobs-only.csv"), hobsOnlyContentExpected.toString(),  StandardCharsets.UTF_8);
        Files.writeString(Paths.get("praise-mds/hob-output-computed-including-cobs-and-solitary-commensals.csv"), episodeContentComputed.toString(),  StandardCharsets.UTF_8);
        Files.writeString(Paths.get("praise-mds/hob-output-computed-hobs-only.csv"), hobsOnlyContentComputed.toString(),  StandardCharsets.UTF_8);

    }


    private static List<String> getRow(EpisodePRAISE episode){

        return List.of(
                episode.getPatientId(),
                formatDate(episode.getEpisodeDate()),
                String.join( episode.getDistinctGerms().stream().sorted().collect(Collectors.joining("+"))),
                String.valueOf((episode).isHOB()),
                String.valueOf((episode).isPolymicrobial()),
                String.valueOf((episode).containsCSC()),
                NN(episode.getClassification())
        );

    }

    private static List<String> getRow(EpisodeImplForTest episode){

        return List.of(
                episode.getPatientId(),
                formatDate(episode.getEpisodeDate()),
                String.join( episode.getDistinctGerms().stream().sorted().collect(Collectors.joining("+"))),
                String.valueOf(episode.getClassification().startsWith("HOB")),
                String.valueOf(episode.isPolymicrobial()),
                String.valueOf(episode.getClassification().contains("CSC")),
                NN(episode.getClassification())
        );

    }

    private static String formatDate(ZonedDateTime zdt){
        return DateTimeFormatter.ofPattern("yyyy/MM/dd").format(zdt);
    }

    private static String NN(Object o){
        if(Objects.isNull(o) || o.toString().length() == 0) return "n/a"; else return o.toString();
    }


}
