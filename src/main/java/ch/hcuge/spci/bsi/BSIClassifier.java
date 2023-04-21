package ch.hcuge.spci.bsi;


import java.util.List;

public interface BSIClassifier<T extends Culture> {

    List<Episode> processCultures(List<T> cultures);

}
