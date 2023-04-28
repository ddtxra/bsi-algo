package ch.hcuge.spci.bsi;

import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL1;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL2;
import ch.hcuge.spci.bsi.constants.classification.BSIClassificationL3;

import java.util.Set;

public interface Episode {

    String getPatientId();

    Boolean isNosocomial();

    BSIClassificationL1 getBSIClassificationL1();

    BSIClassificationL2 getBSIClassificationL2();

    BSIClassificationL3 getBSIClassificationL3();

    Boolean isPolymicrobial();

    Set<String> getDistinctGerms();


}
