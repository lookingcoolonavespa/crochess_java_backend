package com.crochess.backend.controllers;

import com.crochess.backend.daos.GameDao;
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
    public Game get(
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

    @PatchMapping("/{id}")
    boolean makeMove(
            @PathVariable
            int gameid,
            @RequestBody
            MoveDetails moveDetails) {
        Game game = this.dao.get(gameid);
        GameState gs = game.getGameState();

        if (game.getWinner() != null) return false;
        com.crochess.engine0x88.Uci.inputPosition("position startpos moves " + game.getMoves());
        // if the playerId doesn't match up to the active color
        if (com.crochess.engine0x88.GameState.activeColor == Color.W &&
                !Objects.equals(moveDetails.playerId, game.getW_id()) ||
                com.crochess.engine0x88.GameState.activeColor == Color.B &&
                        !Objects.equals(moveDetails.playerId, game.getB_id()))
            return false;

        int move = com.crochess.engine0x88.Uci.algebraToMove(moveDetails.notation);
        if (!com.crochess.engine0x88.GameState.isMoveValid(move)) return false;
        int captureDetails = com.crochess.engine0x88.GameState.makeMove(move);
        if (com.crochess.engine0x88.GameState.isUnforcedDraw()) {

        }
        boolean checkmate = false;
        if (com.crochess.engine0x88.GameState.isCheckmate(com.crochess.engine0x88.GameState.activeColor)) {
            checkmate = true;
            //TODO
        }

        game.setMoves(game.getMoves() + moveDetails.notation);
        String notation = com.crochess.engine0x88.GameState.createMoveNotation(move, captureDetails, checkmate);
        gs.setHistory(gs.getHistory() + notation);
        gs.setFen(com.crochess.engine0x88.GameState.getFen());

        return true;
    }
}
