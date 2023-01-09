package com.crochess.backend.models;

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

    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "game")
    private GameState gameState;
    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, mappedBy = "game")
    private DrawRecord drawRecord;
}
