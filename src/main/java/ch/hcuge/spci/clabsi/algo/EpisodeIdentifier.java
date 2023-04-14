package ch.hcuge.spci.clabsi.algo;

import ch.hcuge.spci.clabsi.model.BloodCulture;
import ch.hcuge.spci.clabsi.model.Episode;

import java.util.List;

public interface EpisodeIdentifier {

    List<Episode> identifyFromBloodCultures(List<BloodCulture> positiveBloodCultures);

}
