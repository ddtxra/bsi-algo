package ch.hcuge.spci.bsi.impl.praise.mapper;

import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.impl.clabsi.model.BloodCulture;
import ch.hcuge.spci.bsi.impl.praise.model.BloodCulturePRAISE;

public class PRAISECultureMapper {

    public static BloodCulturePRAISE mapCulture(Culture culture) {

        if(culture instanceof BloodCulturePRAISE){
            return (BloodCulturePRAISE)culture;
        }

        return new BloodCulturePRAISE(
                culture.getId(), // this.id = id;
                culture.getSampleId(), //  this.sampleId = sampleId;
                culture.getPatientId(), //this.patientId = patientId;
                culture.getStayId(), //this.episodeOfCareId = episodeOfCareId;
                culture.getLaboSampleDate().toLocalDate(), // this.sampleDate = sampleDate;
                culture.getWard(), // this.sampleWardId = sampleWardId;
                null, // this.sampleWardECDCWardClassification
                null, // this.isolateNumber
                null, //microorgSnomedCTCode
                culture.getLaboGermName(), //microorgLocalId
                culture.isLabGermCommensal(),
                "1", //only true from the interface
                null, // attributableWardId
                null, //attributableWardECDCWardClassification
                culture.getStayBeginDate().toLocalDate()); //this.admissionDate = admissionDate;
    }


}
