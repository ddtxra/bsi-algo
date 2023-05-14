package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.exception.BSIException;
import ch.hcuge.spci.bsi.impl.hugv2023.GermType;
import ch.hcuge.spci.bsi.scenarios.model.EpisodeImplForTest;
import ch.hcuge.spci.bsi.scenarios.model.PatientCase;
import ch.hcuge.spci.bsi.scenarios.model.TestCulture;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PatientCaseServiceImpl implements PatientCaseService {

    private String separator = "\t";
    private List<PatientCase> patientCases;

    public PatientCaseServiceImpl() {

    }

    public void loadContent(String fileName) throws IOException, URISyntaxException {
        String content = this.readContent(fileName);
        this.patientCases = this.parseContent(content);
    }


    private String readContent(String file) throws IOException, URISyntaxException {
        Path path = Paths.get(PatientCaseServiceImpl.class.getClassLoader().getResource(file).toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        return new String(fileBytes);
    }

    private void verifyHeader(List<String> columnNames) {
        List<String> expectedHeaders = List.of("patient_id", "stay_begin_date", "labo_sample_date", "labo_germ_name", "labo_commensal", "sample_id");
        if (columnNames.size() != expectedHeaders.size()) {
            throw new RuntimeException("Can't find same headers");
        }

        for (var i = 0; i < expectedHeaders.size(); i++) {
            if (!expectedHeaders.get(i).equalsIgnoreCase(columnNames.get(i))) {
                throw new RuntimeException("Failed the column names are not the same for " + expectedHeaders.get(i) + " and " + columnNames.get(i));
            }
        }
    }

    private Culture convertLineToCulture(String lineContent) {

        String[] values = lineContent.split(this.separator);
        String patient_id = values[0];

        ZonedDateTime beginDateStay = ZonedDateTime.parse(values[1].substring(0, 4) + "-" + values[1].substring(4, 6) + "-" + values[1].substring(6, 8) + "T00:00:00.000Z");
        ZonedDateTime laboSampleDate = ZonedDateTime.parse(values[2] + "T00:00:00.000Z");
        String germName = values[3];

        GermType commensal;
        if((Objects.equals(values[4], "1")) ) {
            commensal =  GermType.COMMENSAL;
        }else if((Objects.equals(values[4], "0")) ) {
            commensal =  GermType.TRUE_PATHOGEN;
        }else {
            throw new BSIException("Commensal value is not compliant with 1 or O, found: " + values[4]);
        }

        if(germName.startsWith("com") && !commensal.equals(GermType.COMMENSAL)){
            throw new BSIException("commensal should be = 1 for germ " + germName  + " for patient " + patient_id);
        }
        if(germName.startsWith("path") && !commensal.equals(GermType.TRUE_PATHOGEN)){
            throw new BSIException("commensal should be = 0 for germ " + germName + " for patient " + patient_id);
        }

        String sampleId = "";
        if(values.length > 5){
            sampleId = values[5];
        }

        return new TestCulture(patient_id, beginDateStay, sampleId, laboSampleDate, germName, commensal);
    }

    private List<PatientCase> parseContent(String content_tsv) {

        var sep = separator != null ? separator : "\t";
        List<String> lines = Arrays.asList(content_tsv.replaceAll("\r", "")
                        .split("\n")).stream()
                .filter(l -> l.trim().length() != 0)
                .toList();

        List<String> column_names = Arrays.asList(lines.get(0).split(sep));
        verifyHeader(column_names);

        List<PatientCase> scenarios = new ArrayList<>();
        boolean expected_line = false;

        if (content_tsv.contains(">")) {
            PatientCase current_scenario = new PatientCase();
            scenarios.add(current_scenario);
            List<Episode> expected_episodes = new ArrayList<>();

            for (int current_line = 1; current_line < lines.size(); current_line++) {
                String line_content = lines.get(current_line).trim();
                if (line_content.startsWith(">")) {
                    expected_line = false;
                    expected_episodes = new ArrayList<>();
                    current_scenario = new PatientCase();
                    scenarios.add(current_scenario);
                } else if (line_content.startsWith("#")) {
                    String comment = line_content.replace("#", "").trim();
                    if (comment.startsWith("⚠️")) {
                        expected_line = false;
                    }
                    if (comment.startsWith("❓")) {
                        expected_line = false;
                    }
                    if (!expected_line && comment.startsWith("expected")) {
                        expected_line = true;
                    } else if (comment.startsWith("expected")) {
                        String[] expectedValues = comment.split("\t");

                        if(expectedValues.length >= 4){

                            EpisodeImplForTest expected_epi = new EpisodeImplForTest(expectedValues[0].split("\\.")[1],
                                    expectedValues[1], expectedValues[2], expectedValues[3]);

                            if(expectedValues.length > 4){
                                expected_epi.setClassification(expectedValues[4]);
                            }

                            if (expected_epi.getPatientId() != null) {
                                expected_episodes.add(expected_epi);
                            }

                            Map<String, List<Episode>> expected_episodes_by_algo = expected_episodes.stream()
                                    .collect(Collectors.groupingBy(e2 -> ((EpisodeImplForTest) e2).getTestAlgoName()));

                            current_scenario.setExpectedEpisodes(expected_episodes_by_algo);
                        }
                    } else {
                        current_scenario.addToDescription(comment);
                    }
                } else {
                    Culture tc = convertLineToCulture(line_content);
                    current_scenario.addPositiveHemoculture(tc);

                    String patientId = current_scenario.getPatientId();
                    if(scenarios.stream().filter(s -> Objects.equals(s.getPatientId(), patientId)).count() > 1){
                        throw new BSIException("Patient " + patientId + "  already defined in " + patientId);
                    }
                }
            }
        } else {

            List<Culture> data = new ArrayList<>();
            for (int current_line = 1; current_line < lines.size(); current_line++) {
                String content = lines.get(current_line).trim();
                Culture culture = convertLineToCulture(content);
                data.add(culture);
            }

            Map<String, List<Culture>> dataGroupedByPatient = data.stream().collect(Collectors.groupingBy(Culture::getPatientId));
            dataGroupedByPatient.forEach((key, value) -> {
                PatientCase scenario = new PatientCase();
                scenario.addToDescription(key);
                value.forEach(scenario::addPositiveHemoculture);
                scenarios.add(scenario);
            });

        }

        return scenarios.stream().filter(s -> s.getCultures().size() > 0).collect(Collectors.toList());
    }

    @Override
    public String getDescription(String patientId) {
        return this.patientCases.stream().filter(s -> s.getCultures().get(0).getPatientId().equals(patientId)).findAny().get().getDescription();
    }


    @Override
    public List<Culture> getCulturesForPatient(String patientId) {
        return this.patientCases.stream().filter(s -> s.getCultures().get(0).getPatientId().equals(patientId)).findAny().get().getCultures();
    }


    @Override
    public List<Episode> getExpectedEpisodesForPatientAndAlgo(String patientId, String algo) {
        return this.patientCases.stream().filter(s -> s.getCultures().get(0).getPatientId().equals(patientId)).findAny().get().getListOfExpectedEpisodesForAlgo(algo);
    }

    @Override
    public List<String> getPatientsIds() {
        return this.patientCases.stream().map(PatientCase::getPatientId).collect(Collectors.toList());
    }


}
