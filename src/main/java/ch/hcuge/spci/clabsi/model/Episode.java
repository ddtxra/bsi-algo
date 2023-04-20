package ch.hcuge.spci.clabsi.model;

import java.util.Set;

public interface Episode {

    Boolean isNosocomial();

    Boolean isPolymicrobial();

    String getClassification();

    Set<String> getDistinctGerms();
}
