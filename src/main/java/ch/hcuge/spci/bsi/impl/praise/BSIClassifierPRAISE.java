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

                //First try to consolidate with existing OB episode (COB or HOB) / COPY STRAINS
                if(!cultureIsProcessed){
                    cultureIsProcessed = attemptToConsolidateWithExistingOBEpisodeUsingNonRepeatIntervalCriteria(episodes, bcp);
                }

                if(!cultureIsProcessed){
                    cultureIsProcessed = attemptToConsolidateWithExistingOBEpisodeUsingPolymicrobialCriteria(episodes, bcp);
                }


                //If it is a commensal
                if(bcp.isCommensal){

                    // check whether there is one already in the 3 days from the same germ
                    List<EpisodePRAISE> matchingEpisodes = episodes.stream()
                            .filter(e -> e.getDistinctGerms().contains(bcp.getLaboGermName()))
                            .filter(e -> Math.abs(ChronoUnit.DAYS.between(e.getEpisodeDate(), bcp.getLaboSampleDate())) < PRAISEParameters.CONTAMINATION_VALID_DAYS).toList();

                    if (matchingEpisodes.size() == 1) {

                        var candidateEpisode = matchingEpisodes.get(0);

                        //Check if it is a solitary commensal
                        if(candidateEpisode.isSolitaryCommensal()){

                            var solitaryCommensal = candidateEpisode.getEvidences().get(0);

                            // if the solitary commensal date is the same as the sample date of the new culture
                            if(solitaryCommensal.getLaboSampleDate().equals(bcp.getLaboSampleDate()) && solitaryCommensal.getSampleId().equals(bcp.getSampleId())){
                                // DISCARD the culture (does not even count as an evidence)
                                cultureIsProcessed = true;
                            }else {
                                //The episode will not be a solitary commensal anymore, it will be a on-set bacteremia
                                candidateEpisode.addSecondEvidenceForCSC(bcp);
                                cultureIsProcessed = true;
                            }

                        } else if(candidateEpisode.isOB()) {

                            if(ChronoUnit.DAYS.between(candidateEpisode.getEpisodeDate(), bcp.getLaboSampleDate()) < PRAISEParameters.NON_REPEAT_INTERVAL_DAYS){
                                candidateEpisode.addCopyStrainEvidence(bcp);
                            }else {
                                episodes.add(new EpisodePRAISE(bcp));
                            }
                            cultureIsProcessed = true;

                        } else {
                            throw new BSIException("Not expecting this state. The episode can only be solitary commensal or OB (onset-bactererimia)");
                        }

                    } else if (matchingEpisodes.size() > 1) {
                        throw new BSIException("Found more " + matchingEpisodes.size() + " than 1 episode for: " + bcp);
                    }

                }

            }

            //Otherwise create a new
            if (!cultureIsProcessed) {
                episodes.add(new EpisodePRAISE(bcp));
            }

        }

        return episodes;

    }


    private boolean attemptToConsolidateWithExistingOBEpisodeUsingPolymicrobialCriteria(List<EpisodePRAISE> episodes, BloodCulturePRAISE bcp){

        boolean consolidated = false;

/*        if(bcp.isCommensal){ //Can't consolidate single bcp that is a commensal
            return false;
        }*/

        //Within 3 days
        List<EpisodePRAISE> matchingEpisodes = episodes.stream()
                .filter(EpisodePRAISE::isOB)
                .filter(e -> !e.getDistinctGerms().contains(bcp.getLaboGermName())) //GERM is not the same!
                .filter(e -> Math.abs(ChronoUnit.DAYS.between(e.getEpisodeDate(), bcp.getLaboSampleDate())) < PRAISEParameters.POLYMICROBIAL_VALID_DAYS).toList();

        if (matchingEpisodes.size() == 1) {

            //Check if it is a onset-bacteremial (HOB or COB doesn't matter)
            matchingEpisodes.get(0).addCopyStrainEvidence(bcp);
            consolidated = true;


        } else if (matchingEpisodes.size() > 1) {
            throw new BSIException("Found more " + matchingEpisodes.size() + " than 1 episode to consolidate using the polymicrobial criteria for culture from patient: " + bcp.getPatientId());
        }

        return consolidated;
    }

    private boolean attemptToConsolidateWithExistingOBEpisodeUsingNonRepeatIntervalCriteria(List<EpisodePRAISE> episodes, BloodCulturePRAISE bcp){

        boolean consolidated = false;

        //Within 3 days
        List<EpisodePRAISE> matchingEpisodes = episodes.stream()
                .filter(EpisodePRAISE::isOB)
                .filter(e -> e.getDistinctGerms().contains(bcp.getLaboGermName())) //GERM is the same!
                .filter(e -> Math.abs(ChronoUnit.DAYS.between(e.getEpisodeDate(), bcp.getLaboSampleDate())) < PRAISEParameters.NON_REPEAT_INTERVAL_DAYS).toList();

        if (matchingEpisodes.size() == 1) {

            //Check if it is a onset-bacteremial (HOB or COB doesn't matter)
            matchingEpisodes.get(0).addCopyStrainEvidence(bcp);
            consolidated = true;

        } else if (matchingEpisodes.size() > 1) {
            throw new BSIException("Found more " + matchingEpisodes.size() + " than 1 episode to consolidate using the non repeat interval criteria for culture of patient: " + bcp.getPatientId());
        }

        return consolidated;
    }



}


