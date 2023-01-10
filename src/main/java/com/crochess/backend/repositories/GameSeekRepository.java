package com.crochess.backend.repositories;

import com.crochess.backend.models.gameSeek.GameSeek;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameSeekRepository extends CrudRepository<GameSeek, Integer> {
    @Modifying
    @Query("select gs.id from GameSeek gs where gs.seeker = :seeker")
    List<Integer> getSeeksBySeeker(
            @Param("seeker")
            String seeker);

    @Modifying
    @Query("delete from GameSeek where seeker = :seeker")
    void deleteSeeksBySeeker(
            @Param("seeker")
            String seeker);
}
