package ch.hcuge.spci.bsi.impl.hugv2023;

import ch.hcuge.spci.BSIUtils;
import ch.hcuge.spci.bsi.Culture;
import ch.hcuge.spci.bsi.constants.GlobalParameters;
import ch.hcuge.spci.bsi.impl.hugv2023.model.PositiveHemoCultureHUGv2023;
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

        Culture culture = new PositiveHemoCultureHUGv2023("1", beginDateStay, sampleDate, "E.Coli", GermType.TRUE_PATHOGEN);

        GlobalParameters.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = true;
        Assert.assertTrue(BSIUtils.isNosocomial(beginDateStay, sampleDate));

        GlobalParameters.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = false;
        Assert.assertFalse(BSIUtils.isNosocomial(beginDateStay, sampleDate));

        GlobalParameters.USE_CALENDAR_DAY_TO_COMPUTE_NOSOCOMIAL = true;
        Assert.assertTrue(BSIUtils.isNosocomial(beginDateStay, sampleDate));

    }


    @Test
    public void shouldParseAndRetrieveCorrectlyDates(){

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/uuuu");
        ZonedDateTime beginDateStay1 = LocalDate.parse("15/04/2023", dtf).atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime beginDateStay2 = ZonedDateTime.parse("2023-04-15T23:38:00.000Z"); //Sun Apr 16 2023 01:38:00 GMT+0200 (Central European Summer Time)
        ZonedDateTime beginDateStay3 = ZonedDateTime.parse("2023-04-15T00:38:00.000Z"); // Sat Apr 15 2023 02:38:00 GMT+0200 (Central European Summer Time)

        PositiveHemoCultureHUGv2023 culture1 = new PositiveHemoCultureHUGv2023("1", beginDateStay1, beginDateStay1, "E.Coli", GermType.TRUE_PATHOGEN);
        PositiveHemoCultureHUGv2023 culture2 = new PositiveHemoCultureHUGv2023("1", beginDateStay2, beginDateStay2, "E.Coli", GermType.TRUE_PATHOGEN);
        PositiveHemoCultureHUGv2023 culture3 = new PositiveHemoCultureHUGv2023("1", beginDateStay3, beginDateStay3, "E.Coli", GermType.TRUE_PATHOGEN);

        Assert.assertEquals(culture1.getStayBeginFormatted("yyyyMMdd"), "20230415");
        Assert.assertEquals(culture2.getStayBeginFormatted("yyyyMMdd"), "20230415");
        Assert.assertEquals(culture3.getStayBeginFormatted("yyyyMMdd"), "20230415");

    }
}
