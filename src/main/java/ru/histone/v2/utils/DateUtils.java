package ru.histone.v2.utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author Gali Alykoff
 */
public class DateUtils implements Serializable {
    public static final int JS_MAX_BOUND_OF_YEAR = 275_761;
    public static final int JS_MIN_BOUND_OF_YEAR = 1_000;
    public static final int MIN_MONTH = 1;
    public static final int MAX_MONTH = 12;
    public static final int MIN_DAY = 1;

    public static int getDaysInMonth(int year, int month) throws IllegalArgumentException {
        final int day = 1;
        final Calendar calendar = new GregorianCalendar(year, month, day);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setLenient(false);
        // check
        calendar.getTimeInMillis();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
