package com.crochess.backend.models;

import com.crochess.backend.types.UserColor;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "crochess", name = "game")
public class Game {
    @Id
    @SequenceGenerator(name = "seq", sequenceName = "crochess.game_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    public Integer id;
    private String w_id;
    private String b_id;
    private int time;
    private int increment;
    private String moves;
    private String result; // checkmate or draw
    private String winner;

    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "game")
    private GameState gameState;

//    public Game(Integer id, String w_id, String b_id, int time, int increment) {
//        this.id = id;
//        this.w_id = w_id;
//        this.b_id = b_id;
//        this.time = time;
//        this.increment = increment;
//        this.moves = null;
//        this.result = null;
//        this.winner = null;
////        this.gameState = null;
//    }

    public UserColor getWinner() {
        return UserColor.of(this.winner);
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
