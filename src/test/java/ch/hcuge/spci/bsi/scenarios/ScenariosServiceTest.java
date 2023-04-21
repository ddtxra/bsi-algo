package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ScenariosServiceTest {


    @Test
    public void shouldReadScenariosFromFile() throws IOException, URISyntaxException {

        ScenariosService scenariosService = new ScenariosServiceImpl();
        ((ScenariosServiceImpl)scenariosService).loadContent("cases.tsv");

        //List<Culture> cultures = scenariosService.loadCulturesForPatient("patient_1002");



    }

}
