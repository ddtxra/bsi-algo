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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BSIApp {

    private final static String CULTURE_FILENAME = "BLOODCULTURES.CSV";

//    private static String folder = "N:\\SPCI\\it-projects\\BSI\\PRAISE\\";
//    private static String cultureFile = "input-from-2010-SAT-3-PRAISE-MDS.csv";
//    private static String outputFile = "output.csv";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static Logger logger = LogManager.getLogger(BSIApp.class);

    public static void main(String args[]) throws IOException {

        if (args.length < 1) {
            logger.error("Please provide a folder path as an argument.");
            System.exit(1);
        }

        String folderPath = args[0];
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            logger.error("The provided path is not a valid folder: " + folder.getAbsolutePath());
            System.exit(1);
        }else {
            logger.info("Reading from: " + folder.getAbsolutePath());
        }

        File bloodCultureFile = new File(folder, CULTURE_FILENAME);
        File patientFile = new File(folder, "PATIENTS.CSV");

        if (!bloodCultureFile.exists()) {
            logger.error("The " + CULTURE_FILENAME + " file does not exist in the provided folder.");
            System.exit(1);
        } else {
            logger.info(CULTURE_FILENAME + " file found in the given folder.");
        }

        if (!patientFile.exists()) {
            logger.warn("The PATIENTS.CSV file does not exist in the provided folder. (ok for now...)");
        }

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        //FOR PRAISE
        List<Culture> cultures = parsePRAISEMDSCultureFile(bloodCultureFile.getAbsolutePath());
        BSIClassifier classifier = new BSIClassifierPRAISE();
        logger.info("- " + cultures.size() + " cultures processed using the " +  classifier.getClass().getSimpleName() + " implementation");
        List<Episode> episodesComputed = classifier.processCultures(cultures);
        logger.info("- " + episodesComputed.size() + " 'episodes?' computed ");
        var hobEpisodes = episodesComputed.stream().filter(e -> ((EpisodePRAISE) e).isHOB()).toList();
        logger.info("--- " + episodesComputed.stream().filter(e -> ((EpisodePRAISE)e).isCOB()).count() + " COB (community-onset bacteremia)");
        logger.info("--- " + episodesComputed.stream().filter(e -> ((EpisodePRAISE)e).isSolitaryCommensal()).count() + " contaminations (solitary commensals)");
        logger.info("--- " + hobEpisodes.size() + " HOB episodes");
        logger.info("----- " + hobEpisodes.stream().filter(Episode::isPolymicrobial).count() + " polymicrobial HOB episodes");
        logger.info("----- " + hobEpisodes.stream().filter(e -> ((EpisodePRAISE)e).containsCSC()).count() + " CSC HOB episodes");
        var outputFile = folder + "/OUTPUT_PRAISE_" + currentTime.format(formatter) + ".CSV";
        saveEpisodeFileForPraise(episodesComputed,outputFile);
        logger.info("Saving file in " +  outputFile + " saved");

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
                Integer isolateNumber = csvRecord.get("isolateNumber").length() > 0 ? Integer.valueOf(csvRecord.get("isolateNumber")): null;
                String microorgSnomedCTCode = csvRecord.get("microorgSnomedCTCode");
                String microorgLocalId = csvRecord.get("microorgLocalId");
                Boolean isCommensal = Boolean.valueOf("1".equals(csvRecord.get("isCSC")));
                Boolean pos_neg = Boolean.valueOf("POS".equals(csvRecord.get("pos_neg")));
                String attributableWardId = csvRecord.get("attributableWardId");
                String attributableWardECDCWardClassification = csvRecord.get("attrWardECDCWardClassification");
                LocalDate admissionDate = LocalDate.parse(csvRecord.get("admHospDate"), formatter);

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
