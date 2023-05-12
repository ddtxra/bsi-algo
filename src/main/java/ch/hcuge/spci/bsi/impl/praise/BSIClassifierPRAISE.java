package ch.hcuge.spci.bsi.impl.praise;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.exception.BSIException;
import ch.hcuge.spci.bsi.impl.praise.mapper.PRAISECultureMapper;
import ch.hcuge.spci.bsi.impl.praise.model.BloodCulturePRAISE;
import ch.hcuge.spci.bsi.impl.praise.model.EpisodePRAISE;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Based on PRAISE specifications
 */
public class BSIClassifierPRAISE implements BSIClassifier {

    @Override
    public List<Episode> processCultures(List<Culture> positiveBloodCultures) {
        if (positiveBloodCultures.size() > 0) {
            return identifiyEpisodes(positiveBloodCultures.stream().map(PRAISECultureMapper::mapCulture).collect(Collectors.toList()));
        }
        return List.of();
    }

    private List<Episode> identifiyEpisodes(List<BloodCulturePRAISE> bloodCultures) {

        List<Episode> episodes = new ArrayList<>();

        //First we group the bloodCultures and filter out only positive results
        final Map<String, List<BloodCulturePRAISE>> positiveBloodCulturesGroupedByPatient = bloodCultures.stream()
                .filter(bc -> bc.pos_neg == Boolean.TRUE || bc.pos_neg == null) // Get only Positive Blood Cultures
                .collect(Collectors.groupingBy(BloodCulturePRAISE::getPatientId));

        //For each patient
        positiveBloodCulturesGroupedByPatient.keySet().forEach(patientId -> {
            List<BloodCulturePRAISE> positiveBloocCulturesForPatient = positiveBloodCulturesGroupedByPatient.get(patientId);
            List<EpisodePRAISE> episodesForPatient = this.identifyEpisodes(positiveBloocCulturesForPatient);
            episodes.addAll(episodesForPatient);

        });

        return episodes;
    }

    private List<EpisodePRAISE> identifyEpisodes(List<BloodCulturePRAISE> culturesForSinglePatient) {

        List<EpisodePRAISE> episodes = new ArrayList<>();
        List<BloodCulturePRAISE> culturesSorted = culturesForSinglePatient.stream().sorted(Comparator.comparing(BloodCulturePRAISE::getLaboSampleDate)).toList();

        for (BloodCulturePRAISE bcp : culturesSorted) {

            boolean cultureIsProcessed = false;

            //If there is no episode, add a new Episode
            if (episodes.isEmpty()) {
                episodes.add(new EpisodePRAISE(bcp));
                cultureIsProcessed = true;
            }

            //Search for existing episode within 14 days with same germ (copy strain)
            if (!cultureIsProcessed) {
                List<EpisodePRAISE> matchingEpisodes = episodes.stream()
                        .filter(e -> e.getDistinctGerms().contains(bcp.getLaboGermName()))
                        .filter(e -> Math.abs(ChronoUnit.DAYS.between(e.getEpisodeDate(), bcp.getLaboSampleDate())) < PRAISEParameters.VALID_NEW_CASES_DAYS).toList();

                if (matchingEpisodes.size() == 1) {
                    matchingEpisodes.get(0).addCopyStrainEvidence(bcp);
                    cultureIsProcessed = true;
                } else if (matchingEpisodes.size() > 1) {
                    throw new BSIException("Found more " + matchingEpisodes.size() + " than 1 episode for: " + bcp);
                }
            }

            //Search for existing episode within 3 days with different germ (polymicrobial)
            if (!cultureIsProcessed) {
                List<EpisodePRAISE> matchingEpisodes = episodes.stream()
                        .filter(e -> !e.getDistinctGerms().contains(bcp.getLaboGermName()))
                        .filter(e -> Math.abs(ChronoUnit.DAYS.between(e.getEpisodeDate(), bcp.getLaboSampleDate())) < PRAISEParameters.POLYMICROBIAL_VALID_DAYS).toList();

                if (matchingEpisodes.size() == 1) {
                    matchingEpisodes.get(0).addPolymicrobialEvidence(bcp);
                    cultureIsProcessed = true;
                } else if (matchingEpisodes.size() > 1) {
                    System.err.println("Found more than one matching episode...");
                }
            }

            //TODO should we treat commensals differently in the "identification phase" ? Since it is only like 0,1,2
            //TODO mnco says that if we have comA and comB separated by 24 hours it is a new episode to investigate. Do you agree?

            //Otherwise create a new
            if (!cultureIsProcessed) {
                episodes.add(new EpisodePRAISE(bcp));
            }

        }

        return episodes;

    }
}


