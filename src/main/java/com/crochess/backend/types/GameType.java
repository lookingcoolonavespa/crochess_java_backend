package com.crochess.backend.types;

import java.util.List;
import java.util.stream.Stream;

public enum GameType {
    BULLET, BLITZ, RAPID, CLASSICAL;

    private static List<String> names() {
        return Stream.of(GameType.values()).map(GameType::name).toList();
    }
    public static boolean contains(String value) {
        return names().contains(value);
    }

    public static GameType of(String type) {
        return switch(type.toUpperCase()) {
            case "BULLET" -> GameType.BULLET;
            case "BLITZ" -> GameType.BLITZ;
            case "RAPID" -> GameType.RAPID;
            case "CLASSICAL" -> GameType.CLASSICAL;
            default -> null;
        };
    }
}
