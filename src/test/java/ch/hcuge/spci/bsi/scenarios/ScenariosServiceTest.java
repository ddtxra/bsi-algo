package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.impl.hugv2023.BSIClassifierHUGv2023;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class ScenariosServiceTest {


    @Test
    public void shouldReadScenariosFromFile() throws IOException, URISyntaxException {

        ScenariosService scenariosService = new ScenariosServiceImpl();
        ((ScenariosServiceImpl)scenariosService).loadContent("cases.tsv");

        BSIClassifierHUGv2023 classifier = new BSIClassifierHUGv2023();
        classifier.processCultures(scenariosService.getScenario())

        //List<Culture> cultures = scenariosService.loadCulturesForPatient("patient_1002");

    }

}
