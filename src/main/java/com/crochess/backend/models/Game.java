package com.crochess.backend.models;

import com.crochess.backend.models.gameSeek.GameSeek;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crochess.game_seq")
    private int id;
    private String w_id;
    private String b_id;
    private int time;
    private int increment;

    @OneToOne(mappedBy = "game")
    private GameState gameState;

    public void setId(int id) {
        this.id = id;
    }
}
