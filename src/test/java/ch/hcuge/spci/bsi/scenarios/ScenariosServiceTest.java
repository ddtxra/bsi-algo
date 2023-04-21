package ch.hcuge.spci.bsi.scenarios;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class ScenariosServiceTest {


    @Test
    public void shouldReadScenariosFromFile() throws IOException, URISyntaxException {

        ScenariosService scenariosService = new ScenariosServiceImpl();
        ((ScenariosServiceImpl)scenariosService).loadContent("cases.tsv");

        //List<Culture> cultures = scenariosService.loadCulturesForPatient("patient_1002");



    }

}
