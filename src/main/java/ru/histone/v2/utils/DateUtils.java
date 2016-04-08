package ru.histone.v2.utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author gali.alykoff on 08/04/16.
 */
public class DateUtils implements Serializable {
    public static int getDaysInMonth(int year, int month) {
        final int day = 1;
        final Calendar calendar = new GregorianCalendar(year, month, day);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
