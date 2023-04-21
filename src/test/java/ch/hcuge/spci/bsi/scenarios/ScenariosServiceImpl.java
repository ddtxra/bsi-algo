package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;
import ch.hcuge.spci.bsi.scenarios.model.EpisodeImplForTest;
import ch.hcuge.spci.bsi.scenarios.model.Scenario;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ScenariosServiceImpl implements ScenariosService {

    private List<Culture> cultures;

    public ScenariosServiceImpl() {

    }

    public void loadContent(String fileName) throws IOException, URISyntaxException {
        String content = this.readContent(fileName);
        System.err.println(content);
    }


    private String readContent(String file) throws IOException, URISyntaxException {
        Path path = Paths.get(ScenariosServiceImpl.class.getClassLoader().getResource(file).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String fileContent = new String(fileBytes);
        return fileContent;
    }

    private void verifyHeader(List<String> columnNames) {
        List<String> expectedHeaders = List.of("patient_id", "stay_begin_date", "labo_sample_date", "labo_germ_name", "labo_commensal");
        if(columnNames.size() != expectedHeaders.size()) {
            throw new RuntimeException("Can't find same headers");
        }

        for(var i=0; i<expectedHeaders.size(); i++){
            if(!expectedHeaders.get(0).equalsIgnoreCase(columnNames.get(0))){
                throw new RuntimeException("Failed");
            }
        }
    }

    private List<Scenario> readContent(String content_tsv, String separator) throws IOException, URISyntaxException {

        var sep = separator != null ? separator : "\t";
        List<String> lines = Arrays.asList(content_tsv.replaceAll("\r", "")
                        .split("\n")).stream()
                .filter(l -> l.trim().length() != 0)
                .toList();

        List<String> column_names = Arrays.asList(lines.get(0).split(sep));

        verifyHeader(column_names);


        List<Scenario> scenarios = new ArrayList<>();
        if (content_tsv.contains(">")) {
            Scenario current_scenario = new Scenario();
            scenarios.add(current_scenario);
            List<Episode> expected_episodes = new ArrayList<>();

            for (int current_line = 1; current_line < lines.size(); current_line++) {
                String line_content = lines.get(current_line).trim();
                if (line_content.startsWith(">")) {
                    boolean expected_line = false;
                    expected_episodes = new ArrayList<>();
                    current_scenario = new Scenario();
                    scenarios.add(current_scenario);
                } else if (line_content.startsWith("#")) {
                    boolean expected_line = false;
                    String comment = line_content.replace("#", "").trim();
                    if (comment.startsWith("!")) {
                        expected_line = false;
                        comment = "<span style='color:red'>" + comment.substring(1).trim() + "</span>";
                    }
                    if (comment.startsWith("?")) {
                        expected_line = false;
                        comment = "<span style='color:orange'>" + comment.substring(1).trim() + "</span>";
                    }
                    if (!expected_line && comment.startsWith("expected")) {
                        expected_line = true;
                    } else if (comment.startsWith("expected")) {
                        String[] expectedValues = comment.split("\t");
                        Episode expected_epi = new EpisodeImplForTest(expectedValues[0].split("\\.")[1],
                                                    expectedValues[1], expectedValues[2], expectedValues[3]);

                        if (expected_epi.getPatientId() != null) {
                            expected_episodes.add(expected_epi);
                        }

                        Map<String, List<Episode>> expected_episodes_by_algo = expected_episodes.stream()
                                .collect(Collectors.groupingBy(e2 -> ((EpisodeImplForTest)e2).getTestAlgoName()));

                        current_scenario.setExpectedEpisodes(expected_episodes_by_algo);

                    } else {
                        current_scenario.addToDescription(comment);
                    }
                } else {
                    boolean expected_line = false;
                    String[] values = line_content.split(separator);
                    //FIXME BloodCulture ph = new BloodCulture(values[0], values[1], values[2], values[3], values[4]);
                    //FIXME current_scenario.addPositiveHemoculture(ph);
                }
            }
        } else {
            boolean expected_line = false;
            List<PositiveHemoCultureHUGv2023> data = new ArrayList<>();

            for (int current_line = 1; current_line < lines.size(); current_line++) {
                String content = lines.get(current_line).trim();
                String[] values = content.split(separator);
                Map<String, String> object = new HashMap<>();
                for (int c = 0; c < column_names.size(); c++) {
                    String col_name = column_names.get(c);
                    object.put(col_name, values[c]);
                }
                //FIXME data.add(new BloodCulture(object.get("patient_id"), object.get("stay_begin_date"), object.get("labo_sample_date"), object.get("labo_germ_name"), object.get("labo_commensal")));
            }

            Map<String, List<PositiveHemoCultureHUGv2023>> dataGroupedByStays = data.stream().collect(Collectors.groupingBy(PositiveHemoCultureHUGv2023::getPatientId));

            dataGroupedByStays.forEach((key, value) -> {
                Scenario scenario = new Scenario();
                scenario.addToDescription(key);
                //FIXME value.forEach(ph -> scenario.addPositiveHemoculture(ph));
                scenarios.add(scenario);
            });

        }

        return scenarios;
    }


    @Override
    public Scenario getScenario(String scenarioId) {
        return null;
    }
}
