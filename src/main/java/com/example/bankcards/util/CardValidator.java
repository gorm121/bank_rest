package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardValidator {

    public boolean luhnCheck(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return false;
        }

        String digits = cardNumber.replaceAll("\\D", "");

        if (digits.length() < 13 || digits.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(digits.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    public static String getCardType(String cardNumber) {
        String digits = cardNumber.replaceAll("\\D", "");

        if (digits.startsWith("4")) return "VISA";
        if (digits.startsWith("5")) return "MASTERCARD";
        if (digits.startsWith("34") || digits.startsWith("37")) return "AMEX";
        if (digits.startsWith("6")) return "DISCOVER";

        return "UNKNOWN";
    }
}