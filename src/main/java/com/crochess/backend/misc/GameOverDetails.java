package com.crochess.backend.misc;

import com.crochess.backend.types.UserColor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameOverDetails {
    private String result; // mate, draw, or time
    private Character winner;

    public UserColor getWinner() {
        return UserColor.of(String.valueOf(this.winner));
    }
}
