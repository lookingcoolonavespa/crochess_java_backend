package com.crochess.backend.controllers;

import com.crochess.backend.daos_and_repos.GameDao;
import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameState;
import com.crochess.engine0x88.types.Color;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.sql.SQLException;
import java.util.Objects;

@RequestMapping("api/game")
@RestController
public class GameController {
    private final GameDao dao;

    public GameController(GameDao dao) {
        this.dao = dao;
    }

    @GetMapping("/{id}")
    public GameState get(
            @PathVariable
            int id) throws SQLException {
        return this.dao.get(id);
    }

    @PostMapping
    public Game insert(
            @RequestBody
            Game game) throws SQLException {
        return this.dao.insert(game);
    }

    @AllArgsConstructor
    private static class MoveDetails {
        String playerId;
        String notation;
    }

    @PatchMapping("/{id")
    boolean makeMove(
            @PathVariable
            int gameid,
            @RequestBody
            MoveDetails moveDetails) throws SQLException {
        Game game = this.dao.get(gameid);

        com.crochess.engine0x88.GameState.loadFen(game.getGameState()
                                                      .getFen());
        if (com.crochess.engine0x88.GameState.activeColor == Color.W &&
                !Objects.equals(moveDetails.playerId, game.getW_id()) ||
                com.crochess.engine0x88.GameState.activeColor == Color.B &&
                        !Objects.equals(moveDetails.playerId, game.getB_id()))
            return false;

        int move = com.crochess.engine0x88.Uci.algebraToMove(moveDetails.notation);
        if (!com.crochess.engine0x88.GameState.isMoveValid(move)) return false;
        com.crochess.engine0x88.GameState.makeMove(move);
        if (com.crochess.engine0x88.GameState.isDraw()) {
            //TODO
        }
        if (com.crochess.engine0x88.GameState.isCheckmate(com.crochess.engine0x88.GameState.activeColor)) {
            //TODO
        }
    }
}
