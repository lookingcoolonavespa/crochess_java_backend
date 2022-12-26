package com.crochess.backend.models;

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
public class GameState {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crochess.gamestate_seq")
    private int id;
    private long time_stamp_at_turn_start;
    private String fen;
    private long w_time;
    private long b_time;
    private String history;
    private boolean w_draw;
    private boolean b_draw;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id", referencedColumnName = "id")
    private Game game;
}
