package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.impl.hugv2023.BSIClassifierHUGv2023;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;

public class PatientCaseServiceTest {


    @Test
    public void shouldReadScenariosFromFile() throws IOException, URISyntaxException {

        PatientCaseServiceImpl testCulturesServices = new PatientCaseServiceImpl();
        testCulturesServices.loadContent("cases.tsv");

        var patientIdsSetSize = new HashSet<>(testCulturesServices.getPatientsIds()).size();
        var patientIdsListSize = new HashSet<>(testCulturesServices.getPatientsIds()).size();

        Assert.assertEquals(patientIdsSetSize, patientIdsListSize);
        BSIClassifier classifier = new BSIClassifierHUGv2023();

        List<Culture> cultures = testCulturesServices.getCulturesForPatient("patient_1001");
        List<Episode> episodes = classifier.processCultures(cultures);

        System.err.println(episodes);


        //List<Culture> cultures = scenariosService.loadCulturesForPatient("patient_1002");

    }

}
