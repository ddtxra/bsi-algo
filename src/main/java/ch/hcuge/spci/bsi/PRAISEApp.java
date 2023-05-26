package ch.hcuge.spci.bsi;

import ch.hcuge.spci.bsi.impl.praise.BSIClassifierPRAISE;
import ch.hcuge.spci.bsi.impl.praise.model.BloodCulturePRAISE;
import ch.hcuge.spci.bsi.impl.praise.model.EpisodePRAISE;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PRAISEApp {

    private static String folder = "N:\\SPCI\\it-projects\\BSI\\PRAISE\\";
    private static String cultureFile = "input-from-2010-SAT-3-PRAISE-MDS.csv";
    private static String outputFile = "SAT-3-PRAISE-episodes_from_2010.csv";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static Logger logger = LogManager.getLogger(PRAISEApp.class);

    public static void main(String args[]) throws IOException {

        //FOR PRAISE
        List<Culture> cultures = parsePRAISEMDSCultureFile(folder + cultureFile);
        BSIClassifier classifier = new BSIClassifierPRAISE();
        logger.info(cultures.size() + " cultures processed");
        List<Episode> episodesComputed = classifier.processCultures(cultures);
        logger.info(episodesComputed.size() + " episodes computed");
        saveEpisodeFileForPraise(episodesComputed,folder + outputFile);
        logger.info("File saved");

    }


    private static List<Culture> parsePRAISEMDSCultureFile(String filePath) throws IOException {

        Path path = Paths.get(filePath);
        String firstLine = Files.lines(path).findFirst().orElse("");
        String[] headerColumns = Arrays.stream(firstLine.split(";"))
                .map(a -> a.replace("\"", ""))
                .toArray(String[]::new);

        List<Culture> cultures = new ArrayList<>();

        try (Reader reader = new FileReader(path.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader(headerColumns).setQuote('"').setDelimiter(";").setSkipHeaderRecord(true).build())) {


            for (CSVRecord csvRecord : csvParser) {

                String cultureId = csvRecord.get(0);
                String sampleId = csvRecord.get("sampleId");
                String patientId = csvRecord.get("patientId");
                String episodeOfCareId = csvRecord.get("episodeOfCareId");
                LocalDate sampleDate = LocalDate.parse(csvRecord.get("sampleDate"), formatter);
                ;
                String sampleWardId = csvRecord.get("sampleWardId");
                String sampleWardECDCWardClassification = csvRecord.get("sampleWardECDCWardClassification");
                Integer isolateNumber = Integer.valueOf(csvRecord.get("isolateNumber"));
                String microorgSnomedCTCode = csvRecord.get("microorgSnomedCTCode");
                String microorgLocalId = csvRecord.get("microorgLocalId");
                Boolean isCommensal = Boolean.valueOf("1".equals(csvRecord.get("isCommensal")));
                Boolean pos_neg = Boolean.valueOf("POS".equals(csvRecord.get("pos_neg")));
                String attributableWardId = csvRecord.get("attributableWardId");
                String attributableWardECDCWardClassification = csvRecord.get("attributableWardECDCWardClassification");
                LocalDate admissionDate = LocalDate.parse(csvRecord.get("admissionDate"), formatter);

                Culture culture = new BloodCulturePRAISE(cultureId, sampleId, patientId, episodeOfCareId, sampleDate, sampleWardId, sampleWardECDCWardClassification,
                        isolateNumber, microorgSnomedCTCode, microorgLocalId, isCommensal, pos_neg, attributableWardId, attributableWardECDCWardClassification, admissionDate);

                cultures.add(culture);

            }
        }

        return cultures;


    }

    private static void saveEpisodeFileForPraise(List<Episode> episodes, String filePath) throws IOException {

        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("patientId", "episodeDate", "microOrganism(s)", "isHob", "containsCSC", "isPolymicrobial", "classification"));
        ) {

            for (Episode e : episodes) {
                EpisodePRAISE ep = (EpisodePRAISE)e;
                csvPrinter.printRecord(ep.getPatientId(), ep.getEpisodeDate().format(formatter), String.join("+", ep.getDistinctGerms()), ep.isHOB(), ep.containsCSC(), ep.isPolymicrobial(), ep.getClassification());
            }
            csvPrinter.flush();
        }

    }

}
