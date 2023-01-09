package com.crochess.backend.models;

import com.crochess.backend.types.UserColor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@DynamicUpdate
@Table(schema = "crochess", name = "gamestate")
public class GameState {
    @Id
    private Integer game_id;
    private long time_stamp_at_turn_start;
    private String fen;
    private long w_time;
    private long b_time;
    private String history;
    private String moves;
    private String result; // mate, draw, or time
    private String winner;

    @OneToOne(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @MapsId
    @ToString.Exclude
    @JsonIgnore
    private Game game;

    public UserColor getWinner() {
        return UserColor.of(this.winner);
    }
}
