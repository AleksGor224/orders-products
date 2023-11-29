package com.demo.orders;

import com.demo.orders.exceptions.ValidationException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CommonUtils {

    private CommonUtils() {
    }

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String mapEpochMillisToTimestamp(long epochMillis) {
        Instant instant = Instant.ofEpochMilli(epochMillis);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        return localDateTime.format(formatter);
    }

    public static void validateUUID(String id, String propertyName) {
        try {
            UUID.fromString(id);
        } catch (Exception ex) {
            throw new ValidationException(String.format("Invalid identifier format for property '%s'. Value: '%s'", propertyName, id));
        }
    }
}
