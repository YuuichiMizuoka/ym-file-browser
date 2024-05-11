package net.vamoscantar.utils.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {

    private StringUtils() {

    }

    public static boolean containsIgnoringCase(String base, String contains) {
        return base.toLowerCase().contains(contains.toLowerCase());
    }

    public static List<String> buildTree(String pathBase, String delimiter) {
        var parts = Arrays.stream(pathBase.split(delimiter)).filter(s -> !s.isBlank()).toList();

        var tree = new ArrayList<String>();
        for (int i = 0; i < parts.size(); i++) {
            if (i == 0) {
                tree.add(delimiter + parts.get(0));
                continue;
            }
            tree.add(tree.get(i - 1) + delimiter + parts.get(i));
        }
        return tree;
    }

    public static boolean isDigits(String s) {
        if (s == null || s.isBlank()) return false;
        return s.chars().allMatch(Character::isDigit);
    }

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static boolean hasText(String rangeHeader) {
        return !isBlank(rangeHeader);
    }
}
