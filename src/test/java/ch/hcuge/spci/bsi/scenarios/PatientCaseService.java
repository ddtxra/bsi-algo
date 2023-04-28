package ch.hcuge.spci.bsi.scenarios;

import ch.hcuge.spci.bsi.Culture;

import java.util.List;

public interface PatientCaseService {

    List<Culture> getCulturesForPatient(String patientId);

    List<String> getPatientsIds();
}
