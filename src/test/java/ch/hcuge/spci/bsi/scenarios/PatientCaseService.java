package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;

import java.util.List;

public interface PatientCaseService {

    String getDescription(String patientId);

    List<Culture> getCulturesForPatient(String patientId);

    List<Episode> getExpectedEpisodesForPatientAndAlgo(String patientId, String algo);

    List<String> getPatientsIds();
}
