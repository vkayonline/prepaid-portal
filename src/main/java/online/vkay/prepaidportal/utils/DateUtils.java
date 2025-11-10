package online.vkay.prepaidportal.utils;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;

@UtilityClass
public final class DateUtils {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    // -----------------------------
    // NOW helpers
    // -----------------------------
    public static LocalDateTime now() {
        return LocalDateTime.now(IST);
    }

    public static LocalDate today() {
        return LocalDate.now(IST);
    }

    // -----------------------------
    // Start / End of Day
    // -----------------------------
    public static LocalDateTime startOfToday() {
        return today().atStartOfDay();
    }

    public static LocalDateTime endOfToday() {
        return today().atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOf(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOf(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    // -----------------------------
    // Date arithmetic
    // -----------------------------
    public static LocalDate todayPlus(Period period) {
        return LocalDate.now().plus(period);
    }

    public static LocalDate todayMinus(Period period) {
        return LocalDate.now().minus(period);
    }

    public static LocalDateTime nowPlus(Duration duration) {
        return LocalDateTime.now().plus(duration);
    }

    public static LocalDateTime nowMinus(Duration duration) {
        return LocalDateTime.now().minus(duration);
    }


    // -----------------------------
    // Formatting
    // -----------------------------
    public static String format(LocalDateTime ldt, String pattern) {
        return ldt.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDate date, String pattern) {
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDateTime ldt) {
        return ldt.format(ISO_DATE_TIME);
    }

    public static String format(LocalDate date) {
        return date.format(ISO_DATE);
    }

    // -----------------------------
    // Parsing
    // -----------------------------
    public static LocalDateTime parseDateTime(String str, String pattern) {
        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String str, String pattern) {
        return LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    // -----------------------------
    // Convert LocalDateTime <-> Epoch if needed
    // -----------------------------
    public static long toEpochMillis(LocalDateTime ldt) {
        return ldt.atZone(IST).toInstant().toEpochMilli();
    }

    public static LocalDateTime fromEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(IST).toLocalDateTime();
    }
}

