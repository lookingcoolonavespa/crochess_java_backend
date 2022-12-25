package com.crochess.backend.repositories;

import com.crochess.backend.models.gameSeek.GameSeek;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GameSeekRepository extends CrudRepository<GameSeek, Integer> {
}
