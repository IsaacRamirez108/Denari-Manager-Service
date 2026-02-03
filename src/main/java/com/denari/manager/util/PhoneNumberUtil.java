package com.denari.manager.util;


import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class PhoneNumberUtil {

    // Updated regex for valid US phone numbers
    // Area code: 2-9 for first digit, 0-9 for second and third
    // Exchange code: 2-9 for first digit, 0-9 for second and third
    // Subscriber number: 0-9 for all four digits
    private static final Pattern US_PHONE_PATTERN = Pattern.compile("^\\+1([2-9]\\d{2})([2-9]\\d{2})(\\d{4})$");

    /**
     * Normalize phone number to E.164 format (+1234567890)
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");

        // Handle US numbers
        if (digitsOnly.length() == 10) {
            return "+1" + digitsOnly;
        } else if (digitsOnly.length() == 11 && digitsOnly.startsWith("1")) {
            return "+" + digitsOnly;
        }

        throw new IllegalArgumentException("Invalid US phone number format");
    }

    /**
     * Validate US phone number format
     */
    public boolean isValidUsPhoneNumber(String phoneNumber) {
        try {
            String normalized = normalizePhoneNumber(phoneNumber);
            return US_PHONE_PATTERN.matcher(normalized).matches();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Format phone number for display (123) 456-7890
     */
    public String formatForDisplay(String phoneNumber) {
        String normalized = normalizePhoneNumber(phoneNumber);
        String digits = normalized.substring(2); // Remove +1
        return String.format("(%s) %s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6, 10));
    }

    /**
     * More lenient validation for testing - allows common test numbers
     */
    public boolean isValidUsPhoneNumberLenient(String phoneNumber) {
        try {
            String normalized = normalizePhoneNumber(phoneNumber);

            // Basic format check: +1 followed by 10 digits
            if (!normalized.matches("^\\+1\\d{10}$")) {
                return false;
            }

            // Extract area code and exchange
            String areaCode = normalized.substring(2, 5);
            String exchange = normalized.substring(5, 8);

            // Area code can't start with 0 or 1
            if (areaCode.startsWith("0") || areaCode.startsWith("1")) {
                return false;
            }

            // Exchange code can't start with 0 or 1
            if (exchange.startsWith("0") || exchange.startsWith("1")) {
                return false;
            }

            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
