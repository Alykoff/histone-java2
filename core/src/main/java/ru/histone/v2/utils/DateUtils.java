package ru.histone.v2.utils;

import org.apache.commons.lang3.StringUtils;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.node.EvalNode;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    public static final String DAY_SYMBOL = "D";
    public static final String WEEK_SYMBOL = "W";
    public static final String MONTH_SYMBOL = "M";
    public static final String YEAR_SYMBOL = "Y";
    public static final String HOUR_SYMBOL = "h";
    public static final String MINUTE_SYMBOL = "m";
    public static final String SECOND_SYMBOL = "s";

    private static final Pattern PATTERN_DELTA_DATE = Pattern.compile("([\\^\\$+-])(\\d*)([DMWYhms])");
    private static final String NEGATIVE_SIGN = "-";
    private static final String POSITIVE_SIGN = "+";
    private static final String START_SIGN = "^";
    private static final String END_SIGN = "$";

    private static final TemporalAdjuster LAST_SECOND_OF_MINUTE_ADJUSTER = temporal -> temporal
            .with(ChronoField.SECOND_OF_MINUTE, 59);

    private static final TemporalAdjuster LAST_MINUTE_OF_HOUR_ADJUSTER = temporal -> temporal
            .with(ChronoField.MINUTE_OF_HOUR, 59).with(LAST_SECOND_OF_MINUTE_ADJUSTER);

    private static final TemporalAdjuster LAST_HOUR_OF_DAY_ADJUSTER = temporal -> temporal
            .with(ChronoField.HOUR_OF_DAY, 23).with(LAST_MINUTE_OF_HOUR_ADJUSTER);

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
            final int num = StringUtils.isNotEmpty(matcher.group(2)) ? Integer.parseInt(matcher.group(2)) : 0;
            final String period = matcher.group(3);
            switch (period) {
                case DAY_SYMBOL:
                    result = applyDayOffset(sign, result, num);
                    break;
                case WEEK_SYMBOL:
                    result = applyWeekOffset(sign, result, num);
                    break;
                case MONTH_SYMBOL:
                    result = applyMonthOffset(sign, result, num);
                    break;
                case YEAR_SYMBOL:
                    result = applyYearOffset(sign, result, num);
                    break;
                case HOUR_SYMBOL:
                    result = applyHourOffset(sign, result, num);
                    break;
                case MINUTE_SYMBOL:
                    result = applyMinuteOffset(sign, result, num);
                    break;
                case SECOND_SYMBOL:
                    result = applySecondOffset(sign, result, num);
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

    public static LocalDateTime createDate(Map<String, EvalNode> map) {
        EvalNode yearNode = map.get("year");
        final int yearValue;
        if (yearNode != null) {
            Optional<Integer> year = EvalUtils.tryPureIntegerValue(yearNode);
            if (year.isPresent()) {
                yearValue = year.get();
            } else {
                return null;
            }
        } else {
            return null;
        }

        EvalNode monthNode = map.get("month");
        final int monthValue;
        if (monthNode != null) {
            ProcessResult res = processValue(monthNode, 0, 13);
            if (res.future != null) {
                return null;
            }
            monthValue = res.value;
        } else {
            return constructDate(yearValue, -1, -1, -1, -1, -1);
        }

        LocalDate currentDate = LocalDate.of(yearValue, monthValue, 1);
        int daysCount = currentDate.getMonth().length(currentDate.isLeapYear());

        EvalNode dayNode = map.get("day");
        final int dayValue;
        if (dayNode != null) {
            ProcessResult res = processValue(dayNode, 0, daysCount + 1);
            if (res.future != null) {
                return null;
            }
            dayValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, -1, -1, -1, -1);
        }

        EvalNode hourNode = map.get("hour");
        final int hourValue;
        if (hourNode != null) {
            ProcessResult res = processValue(hourNode, -1, 24);
            if (res.future != null) {
                return null;
            }
            hourValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, dayValue, -1, -1, -1);
        }

        EvalNode minuteNode = map.get("minute");
        final int minuteValue;
        if (minuteNode != null) {
            ProcessResult res = processValue(minuteNode, -1, 60);
            if (res.future != null) {
                return null;
            }
            minuteValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, dayValue, hourValue, -1, -1);
        }

        EvalNode secondNode = map.get("second");
        final int secondValue;
        if (secondNode != null) {
            ProcessResult res = processValue(secondNode, -1, 60);
            if (res.future != null) {
                return null;
            }
            secondValue = res.value;
        } else {
            return constructDate(yearValue, monthValue, dayValue, hourValue, minuteValue, -1);
        }


        return constructDate(yearValue, monthValue, dayValue, hourValue, minuteValue, secondValue);
    }


    private static ProcessResult processValue(EvalNode node, int minValue, int maxValue) {
        Optional<Integer> value = EvalUtils.tryPureIntegerValue(node);
        if (value.isPresent() && value.get() > minValue && value.get() < maxValue) {
            return new ProcessResult(value.get());
        } else {
            return new ProcessResult(EvalUtils.getValue(null));
        }
    }

    private static LocalDateTime constructDate(int year, int month, int day, int hour, int minute, int second) {
        int monthValue = month < 1 || month > 12 ? 1 : month;
        LocalDate currentDate = LocalDate.of(year, monthValue, 1);
        int dayValue = day < 1 || day > currentDate.getMonth().length(currentDate.isLeapYear()) ? 1 : day;

        int hourValue = hour >= 0 && hour <= 23 ? hour : 0;
        int minuteValue = minute >= 0 && minute <= 59 ? minute : 0;
        int secondValue = second >= 0 && second <= 59 ? second : 0;

        return LocalDateTime.of(year, monthValue, dayValue, hourValue, minuteValue, secondValue);
    }

    private static LocalDateTime applyDayOffset(String sign, LocalDateTime date, int offset) {
        switch (sign) {
            case START_SIGN:
                return date.truncatedTo(ChronoUnit.DAYS);
            case END_SIGN:
                return date.with(LAST_HOUR_OF_DAY_ADJUSTER);
            case NEGATIVE_SIGN:
                return date.minusDays(offset);
            case POSITIVE_SIGN:
                return date.plusDays(offset);
        }
        return date;
    }

    private static LocalDateTime applyWeekOffset(String sign, LocalDateTime date, int offset) {
        switch (sign) {
            case START_SIGN:
                return date.truncatedTo(ChronoUnit.DAYS)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case END_SIGN:
                return date.with(LAST_HOUR_OF_DAY_ADJUSTER)
                        .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            case NEGATIVE_SIGN:
                return date.minusWeeks(offset);
            case POSITIVE_SIGN:
                return date.plusWeeks(offset);
        }
        return date;
    }

    private static LocalDateTime applyMonthOffset(String sign, LocalDateTime date, int offset) {
        switch (sign) {
            case START_SIGN:
                return date.truncatedTo(ChronoUnit.DAYS)
                        .with(TemporalAdjusters.firstDayOfMonth());
            case END_SIGN:
                return date.with(LAST_HOUR_OF_DAY_ADJUSTER)
                        .with(TemporalAdjusters.lastDayOfMonth());
            case NEGATIVE_SIGN:
                return date.minusMonths(offset);
            case POSITIVE_SIGN:
                return date.plusMonths(offset);
        }
        return date;
    }

    private static LocalDateTime applyYearOffset(String sign, LocalDateTime date, int offset) {
        switch (sign) {
            case START_SIGN:
                return date.truncatedTo(ChronoUnit.DAYS)
                        .with(TemporalAdjusters.firstDayOfYear());
            case END_SIGN:
                return date.with(LAST_HOUR_OF_DAY_ADJUSTER)
                        .with(TemporalAdjusters.lastDayOfYear());
            case NEGATIVE_SIGN:
                return date.minusYears(offset);
            case POSITIVE_SIGN:
                return date.plusYears(offset);
        }
        return date;
    }

    private static LocalDateTime applyHourOffset(String sign, LocalDateTime date, int offset) {
        switch (sign) {
            case START_SIGN:
                return date.truncatedTo(ChronoUnit.HOURS);
            case END_SIGN:
                return date.with(LAST_MINUTE_OF_HOUR_ADJUSTER);
            case NEGATIVE_SIGN:
                return date.minusHours(offset);
            case POSITIVE_SIGN:
                return date.plusHours(offset);
        }
        return date;
    }

    private static LocalDateTime applyMinuteOffset(String sign, LocalDateTime date, int offset) {
        switch (sign) {
            case START_SIGN:
                return date.truncatedTo(ChronoUnit.MINUTES);
            case END_SIGN:
                return date.with(LAST_SECOND_OF_MINUTE_ADJUSTER);
            case NEGATIVE_SIGN:
                return date.minusMinutes(offset);
            case POSITIVE_SIGN:
                return date.plusMinutes(offset);
        }
        return date;
    }

    private static LocalDateTime applySecondOffset(String sign, LocalDateTime date, int offset) {
        switch (sign) {
            case NEGATIVE_SIGN:
                return date.minusSeconds(offset);
            case POSITIVE_SIGN:
                return date.plusSeconds(offset);
        }
        return date;
    }

    private static class ProcessResult {
        CompletableFuture<EvalNode> future = null;
        int value = Integer.MIN_VALUE;

        ProcessResult(CompletableFuture<EvalNode> future) {
            this.future = future;
        }

        ProcessResult(int value) {
            this.value = value;
        }
    }
}
