package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.constants.GermType;
import ch.hcuge.spci.bsi.scenarios.model.EpisodeImplForTest;
import ch.hcuge.spci.bsi.scenarios.model.Scenario;
import ch.hcuge.spci.bsi.scenarios.model.TestCulture;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ScenariosServiceImpl implements ScenariosService {

    private List<Culture> cultures;
    private String separator = "\t";

    public ScenariosServiceImpl() {

    }

    public void loadContent(String fileName) throws IOException, URISyntaxException {
        String content = this.readContent(fileName);
        List<Scenario> scenarios = this.parseContent(content);

        System.err.println(scenarios.size());
    }


    private String readContent(String file) throws IOException, URISyntaxException {
        Path path = Paths.get(ScenariosServiceImpl.class.getClassLoader().getResource(file).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String fileContent = new String(fileBytes);
        return fileContent;
    }

    private void verifyHeader(List<String> columnNames) {
        List<String> expectedHeaders = List.of("patient_id", "stay_begin_date", "labo_sample_date", "labo_germ_name", "labo_commensal");
        if (columnNames.size() != expectedHeaders.size()) {
            throw new RuntimeException("Can't find same headers");
        }

        for (var i = 0; i < expectedHeaders.size(); i++) {
            if (!expectedHeaders.get(0).equalsIgnoreCase(columnNames.get(0))) {
                throw new RuntimeException("Failed");
            }
        }
    }

    private Culture convertLineToCulture(String lineContent) {

        String[] values = lineContent.split(this.separator);

        ZonedDateTime beginDateStay = ZonedDateTime.parse(values[1].substring(0, 4) + "-" + values[1].substring(4, 6) + "-" + values[1].substring(6, 8) + "T00:00:00.000Z");
        ZonedDateTime laboSampleDate = ZonedDateTime.parse(values[2] + "T00:00:00.000Z");
        String germName = values[3];
        GermType commensal = (Objects.equals(values[4], "1")) ? GermType.COMMENSAL : GermType.TRUE_PATHOGEN;

        return new TestCulture(values[0], beginDateStay, laboSampleDate, germName, commensal);
    }

    private List<Scenario> parseContent(String content_tsv) {

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
                                .collect(Collectors.groupingBy(e2 -> ((EpisodeImplForTest) e2).getTestAlgoName()));

                        current_scenario.setExpectedEpisodes(expected_episodes_by_algo);

                    } else {
                        current_scenario.addToDescription(comment);
                    }
                } else {

                    Culture tc = convertLineToCulture(line_content);
                    current_scenario.addPositiveHemoculture(tc);

                }
            }
        } else {
            List<Culture> data = new ArrayList<>();
            for (int current_line = 1; current_line < lines.size(); current_line++) {
                String content = lines.get(current_line).trim();
                /*String[] values = content.split(separator);
                Map<String, String> object = new HashMap<>();
                for (int c = 0; c < column_names.size(); c++) {
                    String col_name = column_names.get(c);
                    object.put(col_name, values[c]);
                }*/

                Culture culture = convertLineToCulture(content);
                data.add(culture);
            }

            Map<String, List<Culture>> dataGroupedByStays = data.stream().collect(Collectors.groupingBy(Culture::getPatientId));

            dataGroupedByStays.forEach((key, value) -> {
                Scenario scenario = new Scenario();
                scenario.addToDescription(key);
                value.forEach(scenario::addPositiveHemoculture);
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
