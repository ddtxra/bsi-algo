package ch.hcuge.spci.bsi.impl.hugv2023;

import ch.hcuge.spci.bsi.BSIClassifier;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.impl.hugv2023.mapper.CultureToEpisodeHUGv2023Mapper;
import ch.hcuge.spci.bsi.impl.hugv2023.model.EpisodeHUGv2023;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;
import ch.hcuge.spci.bsi.Episode;
import ch.hcuge.spci.bsi.constants.GlobalParameters;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Based on https://github.com/ddtxra/hob/blob/gh-pages/js/impl/hug-v2023.js
 */
public class BSIClassifierHUGv2023 implements BSIClassifier {

    private Integer VALID_NEW_CASES_DAYS = GlobalParameters.NUMBER_DAYS_FOR_NON_REPEATED_INTERVAL;

    @Override
    public List<Episode> processCultures(List<Culture> positiveBloodCultures) {
        if (positiveBloodCultures.size() > 0) {
            return identifiyEpisodes(positiveBloodCultures.stream().map(CultureToEpisodeHUGv2023Mapper::mapCulture).collect(Collectors.toList()));
        }
        return List.of();
    }

    private List<Episode> identifiyEpisodes(List<PositiveHemoCultureHUGv2023> positiveBloodCultures) {

        var episodes = new ArrayList<Episode>();

        final Map<String, List<PositiveHemoCultureHUGv2023>> pos_hemocultures_by_patient_stay = positiveBloodCultures.stream().collect(
                Collectors.groupingBy(e ->
                        "P-" + e.getPatientId() + "-S-" + e.getStayBeginDate().toInstant().toEpochMilli()));

        pos_hemocultures_by_patient_stay.keySet().forEach(patient_stay -> {
            List<EpisodeHUGv2023> episodesForPatient = new ArrayList<>();
            List<PositiveHemoCultureHUGv2023> pos_hemocultures_for_patient_stay = pos_hemocultures_by_patient_stay.get(patient_stay);
            String formatToBeSorted = "yyyyMMdd";
            Map<String, List<PositiveHemoCultureHUGv2023>> pos_hemocultures_for_patient_stay_by_calendarday = pos_hemocultures_for_patient_stay.stream().collect(Collectors.groupingBy(ph -> ph.getLaboCalendarFormatted(formatToBeSorted)));
            pos_hemocultures_for_patient_stay_by_calendarday.keySet().stream().sorted().forEach(calendar_day -> {
                List<PositiveHemoCultureHUGv2023> pos_hemocultures_for_patient_for_single_days = pos_hemocultures_for_patient_stay_by_calendarday.get(calendar_day);
                var first_pos_hemoculture_for_day = pos_hemocultures_for_patient_for_single_days.get(0);

                var episode = new EpisodeHUGv2023(first_pos_hemoculture_for_day);

                // si il y a plus que 1 germe dans 1 jour il est possible qu'il y a des polymcrobiens
                if (pos_hemocultures_for_patient_for_single_days.size() > 1) {
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


    private List<? extends Episode> consolidateEpisodesBasedOnNonRepatedIntervalsRecursively(List<EpisodeHUGv2023> episodes) {

        var consolidated_count = episodes.size();
        var test_episodes = episodes;
        var fullRescanIfOptimisation = false;
        List<EpisodeHUGv2023> consolidated_episodes;

        do {
            consolidated_episodes = consolidateEpisodesBasedOnNonRepatedIntervals(test_episodes);
            fullRescanIfOptimisation = consolidated_episodes.size() < consolidated_count;
            if (fullRescanIfOptimisation) {
                test_episodes = consolidated_episodes;
                consolidated_count = consolidated_episodes.size();
            }

        } while (fullRescanIfOptimisation);

        return consolidated_episodes;

    }

    private List<EpisodeHUGv2023> consolidateEpisodesBasedOnNonRepatedIntervals(List<EpisodeHUGv2023> episodes) {

        var consolidated_episodes = new ArrayList<EpisodeHUGv2023>();
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
                                Math.abs(ChronoUnit.DAYS.between(current_episode.getEpisodeDate(), consolidated_episode.getEpisodeDate())) < VALID_NEW_CASES_DAYS) {
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
