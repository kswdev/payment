package com.example.backend.util;

import java.time.format.DateTimeFormatter;

public abstract class CustomDateTimeFormatter {
    public static final DateTimeFormatter MYSQL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
