package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;

import java.util.List;

public interface ScenariosService {

    List<Culture> loadCulturesForPatient(String patientId);

}
