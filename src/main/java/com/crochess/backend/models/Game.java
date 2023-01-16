package com.crochess.backend.models;

import com.crochess.backend.misc.WsMessage;
import com.crochess.backend.types.UserColor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private long time_stamp_at_turn_start;
    private String fen;
    private long w_time;
    private long b_time;
    private String history;
    private String moves;
    private String result;
    private Character winner;
    @Version
    Integer version;

    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "game")
    private DrawRecord drawRecord;

    @Getter
    private static final class GameState {
        public GameState(Game game) {
            this.time_stamp_at_turn_start = game.getTime_stamp_at_turn_start();
            this.fen = game.getFen();
            this.w_time = game.getW_time();
            this.b_time = game.getB_time();
            this.history = game.getHistory();
            this.moves = game.getMoves();
        }

        private final long time_stamp_at_turn_start;
        private final String fen;
        private final long w_time;
        private final long b_time;
        private final String history;
        private final String moves;
    }

    @Getter
    private static final class GameOverGameState {
        public GameOverGameState(Game game) {
            this.time_stamp_at_turn_start = game.getTime_stamp_at_turn_start();
            this.fen = game.getFen();
            this.w_time = game.getW_time();
            this.b_time = game.getB_time();
            this.history = game.getHistory();
            this.moves = game.getMoves();
            this.result = game.getResult();
            this.winner = String.valueOf(game.getWinner());
        }

        private final long time_stamp_at_turn_start;
        private final String fen;
        private final long w_time;
        private final long b_time;
        private final String history;
        private final String moves;
        private final String result; // mate, draw, or time
        private final String winner;
    }


    public String toJson(String event) throws JsonProcessingException {
        switch (event) {
            case "init" -> {
                return new ObjectMapper().writeValueAsString(new WsMessage<Game>("init", this));
            }
            case "update" -> {
                return new ObjectMapper().writeValueAsString(new WsMessage<GameState>("update",
                        new GameState(this)));
            }

            case "game over" -> {
                return new ObjectMapper().writeValueAsString(new WsMessage<GameOverGameState>(
                        "game over",
                        new GameOverGameState(this)));
            }
        }
        return "";
    }

    public void setWinner(char winner) {
        this.winner = winner;
    }

    public void setWinner(UserColor winner) {
        switch (winner) {
            case W -> this.winner = 'w';
            case B -> this.winner = 'b';
        }
    }

    private void setVersion() {
    }
}
