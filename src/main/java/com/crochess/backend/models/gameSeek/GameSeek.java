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
@Table(schema = "crochess" , name = "gameseeks")
public class GameSeek {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "crochess.gameseek_seq")
    private int id;
    private String color;
    private int time;
    private int increment;
    private String seeker;

    public GameSeek(
            @JsonProperty("color")
            String color,
            @JsonProperty("time")
            int time,
            @JsonProperty("increment")
            int increment,
            @JsonProperty("seeker")
            String seeker) {
        this.color = color;
        this.time = time;
        this.increment = increment;
        this.seeker = seeker;
    }


    public UserColor getColor() {
        return UserColor.of(this.color);
    };
    public GameType getGameType() {
        if(this.time < 120) return GameType.BULLET;
        else if(this.time < 300) return GameType.BLITZ;
        else if (this.time < 1800) return GameType.RAPID;
        else return GameType.CLASSICAL;
    }
    public String getSeeker() {
        return this.seeker;
    }
}
