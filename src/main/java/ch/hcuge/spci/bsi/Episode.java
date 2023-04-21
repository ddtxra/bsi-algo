package ch.hcuge.spci.bsi;

import java.util.Set;

public interface Episode {

    String getPatientId();

    Boolean isNosocomial();

    Boolean isPolymicrobial();

    String getClassification();

    Set<String> getDistinctGerms();

}
