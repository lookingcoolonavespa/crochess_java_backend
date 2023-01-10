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
@Entity
@DynamicUpdate
@Table(schema = "crochess", name = "game_over_details")
public class GameOverDetails {
    @Id
    private Integer game_id;

    private String result; // mate, draw, or time
    private Character winner;

    @OneToOne(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @MapsId
    @JsonIgnore
    private Game game;

    public UserColor getWinner() {
        return UserColor.of(String.valueOf(this.winner));
    }
}
