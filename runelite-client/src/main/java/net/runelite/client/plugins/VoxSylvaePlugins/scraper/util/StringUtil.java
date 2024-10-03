package net.runelite.client.plugins.VoxSylvaePlugins.scraper.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {
    public static List<String> formatArgs(String input) {
        List<String> result = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return result;
        }

        String[] parts = input.split(",");
        for (String part : parts) {
            String formattedPart = part.trim();
            formattedPart = formattedPart.substring(0, 1).toUpperCase() + formattedPart.substring(1).toLowerCase();
            formattedPart = formattedPart.replace(' ', '_');
            result.add(formattedPart);
        }
        return result;
    }

    public static String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append("_");
            }
        }
        return result.substring(0, result.length() - 1); // Remove the last underscore
    }

    public static String normalizeSearchName(String name) {
        return StringUtil.capitalizeEachWord(name.replace("_", " ").toLowerCase());
    }
}
