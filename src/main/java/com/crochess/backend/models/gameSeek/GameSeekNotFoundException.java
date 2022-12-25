package com.crochess.backend.models.gameSeek;

public class GameSeekNotFoundException extends RuntimeException {
    GameSeekNotFoundException(Long id) {
        super("could not find game seek " + id);
    }
}
