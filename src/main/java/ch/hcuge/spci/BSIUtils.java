package ch.hcuge.spci;

import ch.hcuge.spci.bsi.constants.GlobalParameters;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class BSIUtils {

    public static Boolean isNosocomial(ZonedDateTime stayBeginDate, ZonedDateTime firstLabSampleDate) {

        long daysSinceAdmission;
        if (GlobalParameters.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL) {
            daysSinceAdmission = ChronoUnit.DAYS.between(stayBeginDate.toLocalDate(), firstLabSampleDate.toLocalDate());
        } else {
            daysSinceAdmission = ChronoUnit.DAYS.between(stayBeginDate, firstLabSampleDate);
        }

        return daysSinceAdmission >= 2;

    }


}
