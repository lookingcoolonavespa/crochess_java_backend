package com.crochess.backend.models;

import com.crochess.backend.types.UserColor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "crochess", name = "game")
public class Game {
    @Id
    @SequenceGenerator(name = "seq", sequenceName = "crochess.game_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    private Integer id;
    private String w_id;
    private String b_id;
    private int time;
    private int increment;
    private String moves;
    private String result; // checkmate or draw
    private String winner;

    @OneToOne(cascade = CascadeType.REMOVE, mappedBy = "game")
    private GameState gameState;

    public UserColor getWinner() {
        return UserColor.of(this.winner);
    }
}
