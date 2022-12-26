package com.crochess.backend.daos_and_repos;

import com.crochess.backend.models.gameSeek.GameSeek;
import org.springframework.data.repository.CrudRepository;

public interface GameSeekRepository extends CrudRepository<GameSeek, Integer> {
}
