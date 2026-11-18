package com.example.spendsmart.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtils {
    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter SECTION_HEADER =
            DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private static final DateTimeFormatter MONTH_YEAR =
            DateTimeFormatter.ofPattern("MMMM yyyy");

    private DateUtils() {
    }

    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    public static long startOfMonth(YearMonth yearMonth) {
        return yearMonth.atDay(1).atStartOfDay(ZONE).toInstant().toEpochMilli();
    }

    public static long endOfMonth(YearMonth yearMonth) {
        return yearMonth.plusMonths(1).atDay(1).atStartOfDay(ZONE).toInstant().toEpochMilli();
    }

    public static LocalDate millisToLocalDate(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZONE).toLocalDate();
    }

    public static long localDateToMillis(LocalDate date) {
        return date.atStartOfDay(ZONE).toInstant().toEpochMilli();
    }

    public static String formatDisplayDate(long millis) {
        return millisToLocalDate(millis).format(DISPLAY_DATE);
    }

    public static String formatSectionHeader(long millis) {
        return millisToLocalDate(millis).format(SECTION_HEADER);
    }

    public static String formatMonthYear(YearMonth yearMonth) {
        return yearMonth.format(MONTH_YEAR);
    }

    public static YearMonth yearMonthFromMillis(long millis) {
        return YearMonth.from(millisToLocalDate(millis));
    }
}
