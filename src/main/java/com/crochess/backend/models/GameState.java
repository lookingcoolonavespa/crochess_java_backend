package com.crochess.backend.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
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

    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @MapsId
    @ToString.Exclude
    private Game game;
}
