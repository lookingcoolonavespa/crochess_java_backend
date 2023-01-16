package com.crochess.backend.models;

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
@Table(schema = "crochess", name = "drawrecord")
public class DrawRecord {
    @Id
    private Integer game_id;
    boolean w;
    boolean b;
    @OneToOne(cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @MapsId
    @JsonIgnore
    private Game game;
}
