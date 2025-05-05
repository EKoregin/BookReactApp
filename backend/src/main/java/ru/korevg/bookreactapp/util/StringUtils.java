package ru.korevg.bookreactapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern ISBN_PATTERN = Pattern.compile("\\{(.*?)}");


    public static String extractISBN(String input) {
        Matcher matcher = ISBN_PATTERN.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
