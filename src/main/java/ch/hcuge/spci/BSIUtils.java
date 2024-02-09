package ch.hcuge.spci;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import ch.hcuge.spci.bsi.constants.GlobalParameters;

public class BSIUtils {

        public static Boolean isNosocomial(ZonedDateTime stayBeginDate, ZonedDateTime firstLabSampleDate) {
                return countDaysFromAdmission(stayBeginDate, firstLabSampleDate) >= 2;
        }

        public static long countDaysFromAdmission(ZonedDateTime stayBeginDate, ZonedDateTime firstLabSampleDate) {

                long daysSinceAdmission;
                if (GlobalParameters.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL) {
                        daysSinceAdmission = ChronoUnit.DAYS.between(stayBeginDate.toLocalDate(), firstLabSampleDate.toLocalDate());
                } else {
                        daysSinceAdmission = ChronoUnit.DAYS.between(stayBeginDate, firstLabSampleDate);
                }

                return daysSinceAdmission;

        }

}
