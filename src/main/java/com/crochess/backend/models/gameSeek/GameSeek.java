package com.crochess.backend.models.gameSeek;

import com.crochess.backend.types.UserColor;
import com.crochess.backend.types.GameType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(schema = "crochess", name = "gameseeks")
public class GameSeek {
    @Id
    @SequenceGenerator(name = "seq", sequenceName = "crochess.gameseek_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    private int id;
    private String color;
    private int time;
    private int increment;
    private String seeker;

    public GameSeek(
            String color,
            int time,
            int increment,
            String seeker) {
        this.color = color;
        this.time = time;
        this.increment = increment;
        this.seeker = seeker;
    }


    public UserColor getColor() {
        return UserColor.of(this.color);
    }

    ;

    public GameType getGameType() {
        if (this.time <= 120000) return GameType.BULLET;
        else if (this.time <= 300000) return GameType.BLITZ;
        else if (this.time <= 1800000) return GameType.RAPID;
        else return GameType.CLASSICAL;
    }

    public String getSeeker() {
        return this.seeker;
    }
}
