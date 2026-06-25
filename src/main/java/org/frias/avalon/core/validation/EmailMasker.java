package org.frias.avalon.core.validation;

public class EmailMasker {

    public static String mask(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domainPart;
        }

        return localPart.charAt(0)
                + "***"
                + localPart.substring(localPart.length() - 1)
                + "@"
                + domainPart;
    }
}