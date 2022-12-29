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
@Table(schema = "crochess", name = "gamestate")
public class GameState {
    @Id
    private Integer game_id;
    private long time_stamp_at_turn_start;
    private String fen;
    private long w_time;
    private long b_time;
    private String history;
    private boolean w_draw;
    private boolean b_draw;

    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    @JoinColumn(name = "game_id")
    @MapsId
    private Game game;
}
