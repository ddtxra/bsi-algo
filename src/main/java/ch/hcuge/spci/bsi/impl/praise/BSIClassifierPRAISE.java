package ch.hcuge.spci.bsi.impl.praise;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.impl.hugv2023.HUGv2023Parameters;
import ch.hcuge.spci.bsi.impl.hugv2023.model.EpisodeHUGv2023;
import ch.hcuge.spci.bsi.impl.praise.model.BloodCulturePRAISE;
import ch.hcuge.spci.bsi.impl.praise.model.EpisodePRAISE;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Based on PRAISE specifications
 */
public class BSIClassifierPRAISE implements BSIClassifier {

    @Override
    public List<Episode> processCultures(List<Culture> positiveBloodCultures) {
        if (positiveBloodCultures.size() > 0) {
            return identifiyEpisodes(positiveBloodCultures.stream().map(CultureToEpisodeHUGv2023Mapper::mapCulture).collect(Collectors.toList()));
        }
        return List.of();
    }

    private List<Episode> identifiyEpisodes(List<BloodCulturePRAISE> bloodCultures) {

        //First we group the bloodCultures and filter out only positive results
        final Map<String, List<BloodCulturePRAISE>> positiveBloodCulturesGroupedByPatient = bloodCultures.stream()
                .filter(bc -> bc.pos_neg == Boolean.TRUE || bc.pos_neg == null)
                .collect(Collectors.groupingBy(BloodCulturePRAISE::getPatientId));

        //For each patient
        positiveBloodCulturesGroupedByPatient.keySet().forEach(patientId -> {
            List<EpisodePRAISE> episodesForPatient = new ArrayList<>();
            List<BloodCulturePRAISE> positiveBloocCulturesForPatient = positiveBloodCulturesGroupedByPatient.get(patientId);

            //We group positive blood cultures per calendar days
            Map<String, List<BloodCulturePRAISE>> posBCPerCalDaySortedForPatient = positiveBloocCulturesForPatient.stream()
                    .collect(Collectors.groupingBy(ph -> ph.getLaboCalendarFormatted("yyyyMMdd")));

            posBCPerCalDaySortedForPatient.keySet().stream().sorted().forEach(calendar_day -> {
                List<BloodCulturePRAISE> posBCForCalDayForPatient = posBCPerCalDaySortedForPatient.get(calendar_day);
                var firstPositiveBCForDay = posBCForCalDayForPatient.get(0);

                var episode = new EpisodePRAISE(firstPositiveBCForDay);

                // si il y a plus que 1 germe dans 1 jour il est possible qu'il y a des polymcrobiens
                if (posBCForCalDayForPatient.size() > 1) {
                    // si ces 2 germes sont des germes différents il y a des polymicrobiens
                    var posHemoDifferentGerm = pos_hemocultures_for_patient_for_single_days.stream().filter(ph -> !Objects.equals(ph.getLaboGermName(), first_pos_hemoculture_for_day.getLaboGermName()));
                    posHemoDifferentGerm.forEach(episode::addPolymicrobialEvidence);

                    //si il y a le meme germe le meme jour on le rajoute comme copy strain
                    var posHemoSameGerm = pos_hemocultures_for_patient_for_single_days.stream().filter(ph -> Objects.equals(ph.getLaboGermName(), first_pos_hemoculture_for_day.getLaboGermName()));
                    posHemoSameGerm.forEach(phsg -> {
                        if (phsg != first_pos_hemoculture_for_day) {
                            episode.addCopyStrainEvidence(phsg);
                        }
                    });
                }

                episodesForPatient.add(episode);

            });

            var consolidated_episodes_for_patient = consolidateEpisodesBasedOnNonRepatedIntervalsRecursively(episodesForPatient);
            episodes.addAll(consolidated_episodes_for_patient);

        });

        return episodes;
    }

    private List<EpisodePRAISE> consolidateEpisodesBasedOnNonRepatedIntervals(List<EpisodePRAISE> episodes) {

        var consolidated_episodes = new ArrayList<EpisodePRAISE>();
        episodes.forEach(current_episode -> {
            //se il n'y a pas encore d'episode on rajoute a l array
            if (consolidated_episodes.size() == 0) {
                consolidated_episodes.add(current_episode);
            } else {
                //si il y a déjà un épisode antérieur,
                // on va regarder pour les evidences de l'episode courant
                // si il y a une évidence antérieur avec le même germe
                AtomicBoolean processedEpisode = new AtomicBoolean(false);
                current_episode.getDistinctGerms().forEach(germ -> {
                    consolidated_episodes.forEach(consolidated_episode -> {
                        //si on a un episode consolidé avec le meme germe dans l'interval de temps des 14 jours
                        if (!processedEpisode.get() && consolidated_episode.getDistinctGerms().contains(germ) &&
                                Math.abs(ChronoUnit.DAYS.between(current_episode.getEpisodeDate(), consolidated_episode.getEpisodeDate())) < HUGv2023Parameters.VALID_NEW_CASES_DAYS) {
                            // add all evidences from the current episode to the consolidated (even the ones with different germ)
                            processedEpisode.set(true);
                            current_episode.getEvidences().forEach(consolidated_episode::addEvidenceBasedOnNonRepeatInterval);
                        }
                    });
                });
                if (!processedEpisode.get()) {
                    processedEpisode.set(true);
                    consolidated_episodes.add(current_episode);
                }
            }
        });
        return consolidated_episodes;
    }
}


