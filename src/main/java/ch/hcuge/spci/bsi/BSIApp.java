package ch.hcuge.spci.bsi;

import ch.hcuge.spci.bsi.exception.BSIException;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.text.html.Option;

public class BSIApp {

    private final static String CULTURE_FILENAME = "BLOODCULTURES.CSV";
    private final static String MOVEMENTS_FILENAME = "PATIENTMOVEMENTS.CSV";
    private final static String PATIENT_FILENAME = "PATIENTS.CSV";

//    private static String folder = "N:\\SPCI\\it-projects\\BSI\\PRAISE\\";
//    private static String cultureFile = "input-from-2010-SAT-3-PRAISE-MDS.csv";
//    private static String outputFile = "output.csv";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter sformatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static Logger logger = LogManager.getLogger(BSIApp.class);

    public static void main(String args[]) throws IOException, InterruptedException {

        if (args.length < 1) {
            logger.error("Please provide a folder path as an argument.");
            System.exit(1);
        }


        String folderPath = args[0];
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            logger.error("The provided path is not a valid folder: " + folder.getAbsolutePath() + " !!!!");
            System.exit(1);
        }else {
            logger.info("Reading file from folder: " + folder.getAbsolutePath());
        }

        File bloodCultureFile = new File(folder, CULTURE_FILENAME);
        File patientFile = new File(folder, PATIENT_FILENAME);
        File movementFile = new File(folder, MOVEMENTS_FILENAME);

        if (!bloodCultureFile.exists()) {
            logger.error("The " + CULTURE_FILENAME + " file does not exist in the provided folder.");
            System.exit(1);
        } else {
            logger.info(CULTURE_FILENAME + " file found in the given folder.");
        }

        if (!patientFile.exists()) {
            logger.warn("The " + PATIENT_FILENAME + " file does not exist in the provided folder. (ok for now...)");
        }

        List<Movement> movements = null;
        if (!movementFile.exists()) {
            logger.warn("The " + MOVEMENTS_FILENAME + " file does not exist in the provided folder. (ok for now...)");
        }else {

            logger.info(MOVEMENTS_FILENAME + " file found in the given folder.");
            movements = parsePRAISEMDSPatientMovements(movementFile.getAbsolutePath());
        }

        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        DateTimeFormatter praiseDateFormatted = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        //FOR PRAISE
        List<Culture> cultures = parsePRAISEMDSCultureFile(bloodCultureFile.getAbsolutePath(), movements);

        BSIClassifier classifier = new BSIClassifierPRAISE();
        logger.info("Algorithm implementation: PRAISE (" + classifier.getClass().getSimpleName() + ")");
        logger.info("- " + cultures.size() + " cultures " +  cultures.stream().filter(c -> ((BloodCulturePRAISE)c).isPositiveCulture()).count() + " (positives) processed between " + cultures.stream().min(Comparator.comparing(Culture::getLaboSampleDate)).get().getLaboSampleDate().format(praiseDateFormatted) + " and " + cultures.stream().max(Comparator.comparing(Culture::getLaboSampleDate)).get().getLaboSampleDate().format(praiseDateFormatted)  + " for " + cultures.stream().map(
                Culture::getPatientId).distinct().count() + " patients");

        List<Episode> episodesComputed = classifier.processCultures(cultures);
        logger.info("- " + episodesComputed.size() + " episodes computed for " + episodesComputed.stream().map(Episode::getPatientId).distinct().count() + " patients");
        var hobEpisodes = episodesComputed.stream().filter(e -> ((EpisodePRAISE) e).isHOB()).toList();
        var cobEpisodes = episodesComputed.stream().filter(e -> ((EpisodePRAISE)e).isCOB()).toList();
        logger.info("--- " + cobEpisodes.size() + " COB (community-onset bacteremia) episodes for " + cobEpisodes.stream().map(Episode::getPatientId).distinct().count() + " patients");
        var contaminations = episodesComputed.stream().filter(e -> (((EpisodePRAISE) e).isSolitaryCommensal())).toList();
        logger.info("--- " + contaminations.size() + " Solitary commensals (contaminations) for " + contaminations.stream().map(Episode::getPatientId).distinct().count() + " patients");
        logger.info("--- " + hobEpisodes.size() + " HOB (hospital-onset bacteremia) episodes for " + hobEpisodes.stream().map(Episode::getPatientId).distinct().count() + " patients");
        logger.info("----- " + hobEpisodes.stream().filter(Episode::isPolymicrobial).count() + " polymicrobial HOB episodes");
        logger.info("----- " + hobEpisodes.stream().filter(e -> ((EpisodePRAISE)e).containsCSC()).count() + " CSC HOB episodes");
        var outputFile = folder + "/OUTPUT_ALL_" + currentTime.format(formatter) + ".CSV";
        var hobOutputFile = folder + "/OUTPUT_HOBS_" + currentTime.format(formatter) + ".CSV";
        var hobOutputForPraiseFile = folder + "/OUTPUT_PRAISE_HOBS_TEST.CSV";

        saveEpisodeFileForPraise(episodesComputed,outputFile);
        saveEpisodeFileForPraise(hobEpisodes,hobOutputFile);
        saveEpisodeFileForPraiseInPraiseOutput(hobEpisodes, hobOutputForPraiseFile);


        logger.info("Saving all episodes file in " +  outputFile + " for debug");
        logger.info("Saving HOB episodes file in " +  hobOutputFile);
        logger.info("Saving HOB output format for PRAISE " +  hobOutputForPraiseFile);

    }

    private static List<Movement> parsePRAISEMDSPatientMovements(String filePath) throws IOException {

        Path path = Paths.get(filePath);
        String firstLine = Files.lines(path).findFirst().orElse("");
        String[] headerColumns = Arrays.stream(firstLine.split(";"))
                .map(a -> a.replace("\"", ""))
                .toArray(String[]::new);

        List<Movement> movements = new ArrayList<>();

        try (Reader reader = new FileReader(path.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader(headerColumns).setQuote('"').setDelimiter(";").setSkipHeaderRecord(true).build())) {

            for (CSVRecord csvRecord : csvParser) {
               try {

                    String movementId = csvRecord.get("movementId");
                    String patientId = csvRecord.get("patientId");
                    String wardId = csvRecord.get("wardId");
                    String wardECDCWardClassification = csvRecord.get("wardECDCWardClassification");

                    LocalDate admWardDate = LocalDate.parse(csvRecord.get("admWardDate"), formatter);
                    LocalDate disWardDate = csvRecord.get("disWardDate").length() > 0 ? LocalDate.parse(csvRecord.get("disWardDate"), formatter) : LocalDate.now();
                    LocalDate admHospDate = LocalDate.parse(csvRecord.get("admHospDate"), formatter);
                    LocalDate disHospDate = csvRecord.get("disHospDate").length() > 0 ? LocalDate.parse(csvRecord.get("disHospDate"), formatter) : LocalDate.now();

                    Movement mov1 = new Movement(movementId, patientId, wardId, wardECDCWardClassification, admWardDate, disWardDate, admHospDate, disHospDate);
                    movements.add(mov1);

                }catch (Exception e){
                    logger.error("Failed to parse line: " + csvRecord.getRecordNumber() + " Error msg:" + e.getMessage());
                    throw e;
                }
            }
        }
        return movements;
    }

    private static LocalDate getAdmHospDateBasedOnSample(String patientId, LocalDate sampleDate, List<Movement> movements){
        Set<LocalDate> admDatesFound = movements.stream().filter(m -> m.getPatientId().equals(patientId)).filter(m -> {
            //Between
            if(sampleDate.isAfter(m.getAdmWardDate()) && sampleDate.isBefore(m.getDisWardDate())){
                return true;
            }
            //In the beginning
            if(sampleDate.isEqual(m.getAdmWardDate())){
                return true;
            }
            //In the end
            if(sampleDate.isEqual(m.getDisWardDate())){
                return true;
            }

            return false;

        }).map(e -> e.admHospDate).collect(Collectors.toSet());


        if(admDatesFound.size() == 1){
            return admDatesFound.stream().iterator().next();
        }

        else if(admDatesFound.size() > 1){
            return null;
        } else {
            return null;
        }
    }


    private static List<Culture> parsePRAISEMDSCultureFile(String filePath, List<Movement> movements) throws IOException {

        Path path = Paths.get(filePath);
        String firstLine = Files.lines(path).findFirst().orElse("");
        String[] headerColumns = Arrays.stream(firstLine.split(";"))
                .map(a -> a.replace("\"", ""))
                .toArray(String[]::new);

        List<Culture> cultures = new ArrayList<>();

        try (Reader reader = new FileReader(path.toFile());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader(headerColumns).setQuote('"').setDelimiter(";").setSkipHeaderRecord(true).build())) {

            for (CSVRecord csvRecord : csvParser) {

                try {
                    String cultureId = csvRecord.get(0);
                    String sampleId = csvRecord.get("sampleId");
                    String patientId = csvRecord.get("patientId");
                    LocalDate sampleDate = LocalDate.parse(csvRecord.get("sampleDate"), formatter);
                    String sampleWardId = csvRecord.get("sampleWardId");
                    String sampleWardECDCWardClassification = csvRecord.get("sampleWardECDCWardClassification");
                    Integer isolateNumber = csvRecord.get("isolateNumber").length() > 0 ? Integer.valueOf(csvRecord.get("isolateNumber")): null;
                    String microorgSnomedCTCode = csvRecord.get("microorgSnomedCTCode");
                    String microorgLocalId = csvRecord.get("microorgLocalId");
                    Boolean isCommensal = Boolean.valueOf("1".equals(csvRecord.get("isCSC")));
                    String pos_neg = csvRecord.get("pos_neg");
                    String attributableWardId = csvRecord.get("attributableWardId");
                    String attributableWardECDCWardClassification = csvRecord.get("attrWardECDCWardClassification");

                    LocalDate admissionDate = null;
                    var skip = false;
                    if(csvRecord.get("admHospDate").equals("")){
                        if(movements != null && movements.size() > 0){
                            admissionDate = getAdmHospDateBasedOnSample(patientId, sampleDate, movements);
                        }

                        if(admissionDate == null){
                            String msg = "WARNING !!! Admission date could not be found for patient "  + patientId + " at blood sample date " + csvRecord.get("sampleDate") + ". Culture " + cultureId + " will be skipped !!!";
                            logger.error(msg);
                            skip = true;
                        }
                    }else {
                        admissionDate = LocalDate.parse(csvRecord.get("admHospDate"), formatter);
                    }

                    if(!skip){

                        //episodeOfCareId
                        String episodeOfCareId = csvRecord.get("admHospDate");
                        Culture culture = new BloodCulturePRAISE(cultureId, sampleId, patientId, episodeOfCareId, sampleDate, sampleWardId, sampleWardECDCWardClassification,
                                isolateNumber, microorgSnomedCTCode, microorgLocalId, isCommensal, pos_neg, attributableWardId, attributableWardECDCWardClassification, admissionDate);
                        cultures.add(culture);

                    }

                }catch (Exception e){
                    logger.error("Failed to parse line: " + csvRecord.getRecordNumber() + " Error msg:" + e.getMessage());
                    throw e;
                }
            }
        }

        return cultures;


    }

    private static void saveEpisodeFileForPraise(List<Episode> episodes, String filePath) throws IOException {

        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withDelimiter(';')
                        .withHeader("patientId", "episodeDate", "microOrganism(s)", "isHob", "containsCSC", "isPolymicrobial", "classification"));
        ) {

            for (Episode e : episodes) {
                EpisodePRAISE ep = (EpisodePRAISE)e;
                csvPrinter.printRecord(ep.getPatientId(), ep.getEpisodeDate().format(formatter), String.join("+", ep.getDistinctGerms()), ep.isHOB(), ep.containsCSC(), ep.isPolymicrobial(), ep.getClassification());
            }
            csvPrinter.flush();
        }

    }

    private static void saveEpisodeFileForPraiseInPraiseOutput(List<Episode> episodes, String filePath) throws IOException {

        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withDelimiter(';')
                        .withHeader("hobId", "hobEventDate", "patientId", "patientSex", "patientBirthDate", "patientDeathDate",
                                "bcId", "hobBcStatus", "bcMicroorgLocalId", "bcMicroorgSnomedCTConceptId",
                                "bcMicroorgSpecifiedName", "bcIsCc", "hobPolymicrobial", "hobCc",
                                "hobPathogen", "hobAttrWardId", "hobAttrWardECDCWard", "hobAttrWardECDCWardPatient", "hobAttrWardLocalWardGroup"

                        ));
        ) {

            for (Episode e : episodes) {

                EpisodePRAISE praiseEpisode = (EpisodePRAISE) e;

                String hobId = e.getPatientId() + "_" + e.getEpisodeDate().format(sformatter);
                String hobEventDate = e.getEpisodeDate().format(formatter);
                String patientId = e.getPatientId();
                String patientSex = "";
                String patientBirthDate = "";
                String patientDeathDate = "";
                String hobBcStatus = "MICROORG_EVENT";
                Boolean hobPolymicrobial = e.isPolymicrobial(); //boolean	0 (no); 1 (yes)	[1..n]	if polymicrobial -> 1 = True = Yes
                Boolean hobCc = ((EpisodePRAISE) e).containsCSC(); //boolean	0 (no); 1 (yes)	[1..n]	HOB contains a CSC
                Boolean hobPathogen = ((EpisodePRAISE) e).containsPathogen(); //boolean	0 (no); 1 (yes)	[1..n]	HOB contains a recognized pathogen
                String hobAttrWardId = ""; //stringWardID to which HOB is attributed
                String hobAttrWardECDCWard = ""; // tring	value_set (ECDC classification list)	[1..n]	Ward type to what HOB is attributed: Consensus = ward type, patient specialty = optional.
                String hobAttrWardECDCWardPatient = ""; // string	value_set (ECDC classification list)	[0..n]	Optional.
                String hobAttrWardLocalWardGroup = ""; // string	value_set (locally defined)	[0..n]	Optional.


                if(e.getDistinctGerms().size() == 1){
                    Optional<BloodCulturePRAISE> firstEvidenceForGerm = ((EpisodePRAISE) e).getEvidences().stream().findFirst();
                    String bcId = firstEvidenceForGerm.get().getId();
                    String bcMicroorgLocalId = firstEvidenceForGerm.get().microorgLocalId;
                    String bcMicroorgSnomedCTConceptId = null;
                    String bcMicroorgSpecifiedName = null;
                    Boolean bcIsCc = firstEvidenceForGerm.get().isCommensal;

                    String regex = "(.*?)\\s*\\((\\d+)\\)";
                    Matcher matcher = Pattern.compile(regex).matcher(firstEvidenceForGerm.get().microorgLocalId);

                    if(matcher.find()){
                        bcMicroorgLocalId = matcher.group(1);
                        bcMicroorgSpecifiedName = matcher.group(2);
                    }else {
                        bcMicroorgLocalId = "";
                        bcMicroorgSpecifiedName = "";

                    }


                    csvPrinter.printRecord(hobId, hobEventDate, patientId, patientSex, patientBirthDate, patientDeathDate, bcId, hobBcStatus, bcMicroorgLocalId, bcMicroorgSnomedCTConceptId, bcMicroorgSpecifiedName, bcIsCc, hobPolymicrobial, hobCc, hobPathogen, hobAttrWardId, hobAttrWardECDCWard, hobAttrWardECDCWardPatient, hobAttrWardLocalWardGroup);

                }else{
                    for(String germ : e.getDistinctGerms()){
                        Optional<BloodCulturePRAISE> firstEvidenceForGerm = ((EpisodePRAISE) e).getEvidences().stream().filter(evi -> evi.getLaboGermName().equals(germ)).findFirst();
                        if(firstEvidenceForGerm.isPresent()){

                            String bcId = firstEvidenceForGerm.get().getId();
                            String bcMicroorgLocalId ;
                            String bcMicroorgSpecifiedName;
                            String bcMicroorgSnomedCTConceptId = "";

                            String regex = "(.*?)\\s*\\((\\d+)\\)";
                            Matcher matcher = Pattern.compile(regex).matcher(firstEvidenceForGerm.get().microorgLocalId);

                            if(matcher.find()){
                                bcMicroorgLocalId = matcher.group(1);
                                bcMicroorgSpecifiedName = matcher.group(2);
                            }else {
                                bcMicroorgLocalId = "";
                                bcMicroorgSpecifiedName = "";

                            }


                            Boolean bcIsCc = firstEvidenceForGerm.get().isCommensal;

                            csvPrinter.printRecord(hobId, hobEventDate, patientId, patientSex, patientBirthDate, patientDeathDate, bcId, hobBcStatus, bcMicroorgLocalId, bcMicroorgSnomedCTConceptId, bcMicroorgSpecifiedName, bcIsCc, hobPolymicrobial, hobCc, hobPathogen, hobAttrWardId, hobAttrWardECDCWard, hobAttrWardECDCWardPatient, hobAttrWardLocalWardGroup);
                        }else {
                            throw new BSIException("Could not find germ " + germ);
                        }
                    }
                }
            }
            csvPrinter.flush();
        }

    }

}
