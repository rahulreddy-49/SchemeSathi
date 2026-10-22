package com.schemesathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SchemeSathiApplication {

    static {
        // Normalize legacy Windows Indian Standard Time identifier "Asia/Calcutta" to canonical IANA "Asia/Kolkata"
        // to prevent PostgreSQL JDBC connection rejection (FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta").
        if ("Asia/Calcutta".equalsIgnoreCase(TimeZone.getDefault().getID())) {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SchemeSathiApplication.class, args);
    }
}

