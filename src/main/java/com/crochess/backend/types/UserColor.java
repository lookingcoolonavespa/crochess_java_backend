package com.crochess.backend.types;

import java.util.List;
import java.util.stream.Stream;

public enum UserColor {
    W, B, RANDOM;

    private static List<String> names() {
        return Stream.of(UserColor.values())
                     .map(UserColor::name)
                     .toList();
    }

    public static boolean contains(String value) {
        return names().contains(value);
    }

    public static UserColor of(String type) {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "W" -> UserColor.W;
            case "B" -> UserColor.B;
            case "RANDOM" -> UserColor.RANDOM;
            default -> null;
        };
    }
}
