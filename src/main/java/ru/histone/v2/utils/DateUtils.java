package ru.histone.v2.utils;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.node.EvalNode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gali Alykoff
 */
public class DateUtils implements Serializable {
    public static final int JS_MAX_BOUND_OF_YEAR = 275_761;
    public static final int JS_MIN_BOUND_OF_YEAR = 1_000;
    public static final int MIN_MONTH = 1;
    public static final int MAX_MONTH = 12;
    public static final int MIN_DAY = 1;

    private static final Pattern PATTERN_DELTA_DATE = Pattern.compile("([+-])(\\d+)([DMYhms])");
    private static final String NEGATIVE_SIGN = "-";
    private static final String DAY_SYMBOL = "D";
    private static final String MONTH_SYMBOL = "M";
    private static final String YEAR_SYMBOL = "Y";
    private static final String HOUR_SYMBOL = "h";
    private static final String MINUTE_SYMBOL = "m";
    private static final String SECOND_SYMBOL = "s";

    public static int getDaysInMonth(int year, int month) throws IllegalArgumentException {
        final int day = 1;
        final Calendar calendar = new GregorianCalendar(year, month, day);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setLenient(false);
        // check
        calendar.getTimeInMillis();
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static LocalDateTime applyOffset(LocalDateTime date, String offset) {
        LocalDateTime result = date;
        final Matcher matcher = PATTERN_DELTA_DATE.matcher(offset);
        while (matcher.find()) {
            final String sign = matcher.group(1);
            final Integer num = Integer.parseInt(matcher.group(2)) * (sign.equals(NEGATIVE_SIGN) ? -1 : 1);
            final String period = matcher.group(3);
            switch (period) {
                case DAY_SYMBOL:
                    result = result.plusDays(num);
                    break;
                case MONTH_SYMBOL:
                    result = result.plusMonths(num);
                    break;
                case YEAR_SYMBOL:
                    result = result.plusYears(num);
                    break;
                case HOUR_SYMBOL:
                    result = result.plusHours(num);
                    break;
                case MINUTE_SYMBOL:
                    result = result.plusMinutes(num);
                    break;
                case SECOND_SYMBOL:
                    result = result.plusSeconds(num);
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    public static Map<String, EvalNode> createMapFromDate(LocalDateTime date) {
        final Map<String, EvalNode> res = new LinkedHashMap<>();
        res.put("day", EvalUtils.createEvalNode(date.getDayOfMonth()));
        res.put("month", EvalUtils.createEvalNode(date.getMonthValue()));
        res.put("year", EvalUtils.createEvalNode(date.getYear()));
        res.put("hour", EvalUtils.createEvalNode(date.getHour()));
        res.put("minute", EvalUtils.createEvalNode(date.getMinute()));
        res.put("second", EvalUtils.createEvalNode(date.getSecond()));

        return res;
    }
}
