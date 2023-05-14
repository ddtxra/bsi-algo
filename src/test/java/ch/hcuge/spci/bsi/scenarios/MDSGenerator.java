package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
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

        StringBuilder cultureContent = new StringBuilder();
        var headers = List.of("secenario#", "id", "sampleId", "patientId", "episodeOfCareId", "sampleDate", "sampleWardId",	"sampleWardECDCWardClassification",	"isolateNumber",	"microorgSnomedCTCode",	"microorgLocalId", "isCSC", "pos_neg",	"attributableWardId",	"attributableWardECDCWardClassification", "admissionDate");
        cultureContent.append(String.join("\t", headers));
        cultureContent.append("\n");

        testCulturesServices.getPatientsIds().forEach(pId -> {

            List<Culture> cultures = testCulturesServices.getCulturesForPatient(pId);
            List<Episode> expectedEpisodes = testCulturesServices.getExpectedEpisodesForPatientAndAlgo(pId, "PRAISE");

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



        });

        Path path = Paths.get("cultures.txt");
        try {
            Files.write(path, cultureContent.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
