package org.babi.backend.common.util;

import jakarta.annotation.Nullable;

public class StringUtils {

    @Nullable
    public static String toCamelCase(String text) {
        if (text == null) {
            return null;
        }
        String[] words = text.split("[\\W_]+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                word = word.isEmpty() ? word : word.toLowerCase();
            } else {
                word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
            }
            builder.append(word);
        }
        return builder.toString();
    }

}
