package ch.hcuge.spci.bsi;

import java.time.ZonedDateTime;
import java.util.Set;

public interface Episode {

    String getPatientId();

    Boolean isNosocomial();

    ZonedDateTime getEpisodeDate();

    String getClassification();

    Boolean isPolymicrobial();

    Set<String> getDistinctGerms();


}
