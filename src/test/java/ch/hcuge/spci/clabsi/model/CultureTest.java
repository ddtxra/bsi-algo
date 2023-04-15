package ch.hcuge.spci.clabsi.model;

import ch.hcuge.spci.clabsi.AlgoConstants;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;

public class CultureTest {

    @Test
    public void shouldGiveDifferentResultsForNosocomialWhenTheHemocultureIsMoreThan24ButLessThan48(){

        ZonedDateTime beginDateStay = ZonedDateTime.parse("2023-04-15T20:00:00.000Z");
        ZonedDateTime sampleDate = ZonedDateTime.parse("2023-04-17T08:00:00.000Z");

        Culture culture = new BloodCulture("1", beginDateStay, sampleDate, "E.Coli", GermType.TRUE_PATHOGEN);

        AlgoConstants.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = true;
        Assert.assertEquals(culture.isNosocomial(), true);

        AlgoConstants.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = false;
        Assert.assertEquals(culture.isNosocomial(), false);

        AlgoConstants.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = true;
        Assert.assertEquals(culture.isNosocomial(), true);

    }
}
