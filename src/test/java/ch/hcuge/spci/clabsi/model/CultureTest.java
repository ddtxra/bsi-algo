package ch.hcuge.spci.clabsi.model;

import ch.hcuge.spci.clabsi.AlgoConstants;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CultureTest {

    @Test
    public void shouldGiveDifferentResultsForNosocomialWhenTheHemocultureIsMoreThan24ButLessThan48(){

        ZonedDateTime beginDateStay = ZonedDateTime.parse("2023-04-15T20:00:00.000Z");
        ZonedDateTime sampleDate = ZonedDateTime.parse("2023-04-17T08:00:00.000Z");

        Culture culture = new BloodCulture("1", beginDateStay, sampleDate, "E.Coli", GermType.TRUE_PATHOGEN);

        AlgoConstants.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = true;
        Assert.assertTrue(culture.isNosocomial());

        AlgoConstants.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = false;
        Assert.assertFalse(culture.isNosocomial());

        AlgoConstants.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = true;
        Assert.assertTrue(culture.isNosocomial());

    }


    @Test
    public void shouldParseAndRetrieveCorrectlyDates(){

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        ZonedDateTime beginDateStay1 = LocalDate.parse("15/04/2023", dtf).atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime beginDateStay2 = ZonedDateTime.parse("2023-04-15T23:38:00.000Z"); //Sun Apr 16 2023 01:38:00 GMT+0200 (Central European Summer Time)
        ZonedDateTime beginDateStay3 = ZonedDateTime.parse("2023-04-15T00:38:00.000Z"); // Sat Apr 15 2023 02:38:00 GMT+0200 (Central European Summer Time)

        Culture culture1 = new BloodCulture("1", beginDateStay1, beginDateStay1, "E.Coli", GermType.TRUE_PATHOGEN);
        Culture culture2 = new BloodCulture("1", beginDateStay2, beginDateStay2, "E.Coli", GermType.TRUE_PATHOGEN);
        Culture culture3 = new BloodCulture("1", beginDateStay3, beginDateStay3, "E.Coli", GermType.TRUE_PATHOGEN);

        Assert.assertEquals(culture1.getStayBeginCalendarDayISO(), "15/04/2023");
        Assert.assertEquals(culture2.getStayBeginCalendarDayISO(), "15/04/2023");
        Assert.assertEquals(culture3.getStayBeginCalendarDayISO(), "15/04/2023");

    }
}
