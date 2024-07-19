package com.telda;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static com.telda.CronStringHelper.getMillisToNextCron;
import static org.junit.jupiter.api.Assertions.*;

class CronStringHelperTest {
    final long nowInMillis = 1721347200000L; // Friday 19/07/2024 00:00:00 UTC
    final Date now = new Date(nowInMillis);

    final long SECOND = 1000L;
    final long MINUTE = SECOND * 60;
    final long HOUR = MINUTE * 60;
    final long DAY = HOUR * 24;

    @Test
    void testNow() {
        assertEquals(0, getMillisToNextCron(now, "* * * * *"));
        assertEquals(0, getMillisToNextCron(now, "0 0 19 7 5"));
        assertEquals(0, getMillisToNextCron(now, "0 0 19 7 *"));
        assertEquals(0, getMillisToNextCron(now, "0 0 19 * *"));
        assertEquals(0, getMillisToNextCron(now, "0 0 * * *"));
        assertEquals(0, getMillisToNextCron(now, "0 * * * *"));
    }

    @Test
    void testAfterMinute() {
        assertEquals(MINUTE, getMillisToNextCron(now, "1 0 19 7 5"));
        assertEquals(MINUTE, getMillisToNextCron(now, "1 0 19 7 *"));
        assertEquals(MINUTE, getMillisToNextCron(now, "1 0 19 * *"));
        assertEquals(MINUTE, getMillisToNextCron(now, "1 0 * * *"));
        assertEquals(MINUTE, getMillisToNextCron(now, "1 * * * *"));
    }

    @Test
    void testAfterHour() {
        assertEquals(HOUR, getMillisToNextCron(now, "0 1 19 7 5"));
        assertEquals(HOUR, getMillisToNextCron(now, "0 1 19 7 *"));
        assertEquals(HOUR, getMillisToNextCron(now, "0 1 19 * *"));
        assertEquals(HOUR, getMillisToNextCron(now, "0 1 * * *"));
        assertEquals(HOUR, getMillisToNextCron(now, "* 1 * * *"));
    }

    @Test
    void testAfterDay() {
        assertEquals(DAY, getMillisToNextCron(now, "0 0 20 7 5"));
        assertEquals(DAY, getMillisToNextCron(now, "0 0 20 7 *"));
        assertEquals(DAY, getMillisToNextCron(now, "0 0 20 * *"));
        assertEquals(DAY, getMillisToNextCron(now, "* 0 20 * *"));
        assertEquals(DAY, getMillisToNextCron(now, "0 * 20 * *"));
        assertEquals(DAY, getMillisToNextCron(now, "* * 20 * *"));
    }

    @Test
    void testNextMonday() {
        long tillNextMonday = 1721606400000L - nowInMillis;
        assertEquals(tillNextMonday, getMillisToNextCron(now, "* * * * 1"));
        assertEquals(tillNextMonday + MINUTE, getMillisToNextCron(now, "1 * * * 1"));
        assertEquals(tillNextMonday + MINUTE + HOUR, getMillisToNextCron(now, "1 1 * * 1"));
    }

    @Test
    void testNextMonth() {
        long tillNextMonth = 1722470400000L - nowInMillis; // Thursday, 01/08/2024 00:00:00 UTC
        assertEquals(tillNextMonth, getMillisToNextCron(now, "* * * 8 *"));
        assertEquals(tillNextMonth, getMillisToNextCron(now, "0 * * 8 *"));
        assertEquals(tillNextMonth, getMillisToNextCron(now, "* 0 * 8 *"));
        assertEquals(tillNextMonth, getMillisToNextCron(now, "* * 1 8 *"));
        assertEquals(tillNextMonth, getMillisToNextCron(now, "0 0 1 8 *"));
    }

    @Test
    void testValueListSeparator() {
        assertEquals(MINUTE, getMillisToNextCron(now, "1,5 * * * *"));
        assertEquals(HOUR, getMillisToNextCron(now, "0 1,2 * * *"));
        assertEquals(DAY, getMillisToNextCron(now, "0 0 20,25 * *"));
    }

    @Test
    void testRangeOfValues() {
        assertEquals(MINUTE, getMillisToNextCron(now, "1-12 * * * *"));
        assertEquals(HOUR, getMillisToNextCron(now, "0 1-10 * * *"));
        assertEquals(DAY, getMillisToNextCron(now, "0 0 20-22 * *"));
    }

    @Test
    void testStepValues() {
        long tillTarget = 1721520000000L - nowInMillis; // Sunday, 21/07/2024 00:00:00 UTC
        assertEquals(tillTarget, getMillisToNextCron(now, "0 0 */3 * *"));
    }

    @Test
    void testInvalidCron() {
        List<String> list = List.of("invalid",
                "0 0 25 * *",
                "60 0 * * *",
                "* * * *",
                "",
                "* * * * * *",
                "0 0 -1 * *",
                "0 0 1-32 * *",
                "0 0 1 13 *",
                "0 0 1 0 *",
                "0 0 1 * 8",
                "* * * January *",
                "*/61 * * * *",
                "0/24 * * * *",
                "0 0 32 * *");
        for(String expr: list) {
            assertThrows(IllegalArgumentException.class, () -> getMillisToNextCron(now, expr));
        }
    }
}