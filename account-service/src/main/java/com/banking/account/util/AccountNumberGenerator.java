package com.banking.account.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AccountNumberGenerator {

    private static final String BANK_CODE = "4567";
    private final Random random = ThreadLocalRandom.current();

    /**
     * Generates a 16-digit account number in format: BANK_CODE + YYYYMM + 6-digit random
     * Example: 4567202501938472
     */
    public String generate() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomPart = String.format("%06d", random.nextInt(999999));
        String rawNumber = BANK_CODE + datePart + randomPart;
        return rawNumber.substring(0, 16);
    }
}
