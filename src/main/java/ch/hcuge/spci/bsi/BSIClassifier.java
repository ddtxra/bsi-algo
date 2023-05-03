package ch.hcuge.spci.bsi;


import java.util.List;

public interface BSIClassifier{

    /**
     * Identify and classify episodes based on cultures
     * @param cultures
     * @return
     */
    List<Episode> processCultures(List<Culture> cultures);

}
