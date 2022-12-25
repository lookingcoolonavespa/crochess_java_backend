package com.crochess.backend.types;

import java.util.List;
import java.util.stream.Stream;

public enum Color {
    W,B, RANDOM;

    private static List<String> names() {
        return Stream.of(Color.values()).map(Color::name).toList();
    }
    public static boolean contains(String value) {
        return names().contains(value);
    }

    public static Color of(String type) {
        return switch(type.toUpperCase()) {
            case "WHITE" -> Color.W;
            case "BLACK" -> Color.B;
            case "RANDOM" -> Color.RANDOM;
            default -> null;
        };
    }
}
