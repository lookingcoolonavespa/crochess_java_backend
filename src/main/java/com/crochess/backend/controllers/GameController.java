package com.crochess.backend.controllers;

import com.crochess.backend.daos.GameDao;
import com.crochess.backend.misc.WsMessage;
import com.crochess.backend.models.Game;
//import com.crochess.backend.models.GameState;
import com.crochess.backend.models.gameSeek.GameSeek;
import com.crochess.engine0x88.types.Color;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@RequestMapping("api/game")
@RestController
public class GameController {
    private final GameDao dao;
    private final SimpMessagingTemplate template;

    public GameController(GameDao dao, SimpMessagingTemplate template) {
        this.dao = dao;
        this.template = template;
    }

    @GetMapping("/{id}")
    public Game get(
            @PathVariable
            int id) {
        return this.dao.get(id);
    }

    @PostMapping
    public Game insert(
            @RequestBody
            Game game) throws JsonProcessingException {
        List<Integer> deletedGameSeeks = this.dao.insert(game);
        String json = new ObjectMapper().writeValueAsString(new WsMessage<List<Integer>>("delete", deletedGameSeeks));
        this.template.convertAndSend("/topic/api/gameseeks", json);

        template.convertAndSendToUser(game.getW_id(), "/queue/gameseeks", game.getId());
        template.convertAndSendToUser(game.getB_id(), "/queue/gameseeks", game.getId());
        return game;
    }

    @AllArgsConstructor
    public static class MoveDetails {
        String playerId;
        String notation;
    }

//    @MessageMapping("/{id}")
//    public GameState makeMove(
//            @DestinationVariable
//            int id,
//            @Payload
//            MoveDetails moveDetails) {
//        Consumer<Game> patchGame = (Game game) -> {
//            GameState gs = game.getGameState();
//            System.out.println(gs);
//            if (game.getWinner() != null) return;
//
//            if (game.getMoves() == null) com.crochess.engine0x88.Uci.inputPosition("position startpos");
//            else com.crochess.engine0x88.Uci.inputPosition("position startpos moves " + game.getMoves());
//            // if the playerId doesn't match up to the active color
//            if (com.crochess.engine0x88.GameState.activeColor == Color.W &&
//                    !Objects.equals(moveDetails.playerId, game.getW_id()) ||
//                    com.crochess.engine0x88.GameState.activeColor == Color.B &&
//                            !Objects.equals(moveDetails.playerId, game.getB_id()))
//                return;
//
//            int move = com.crochess.engine0x88.Uci.algebraToMove(moveDetails.notation);
//            if (!com.crochess.engine0x88.GameState.isMoveValid(move)) return;
//            int captureDetails = com.crochess.engine0x88.GameState.makeMove(move);
//
//            // player loses right to claim draw after move is made
//            gs.setB_draw(false);
//            gs.setW_draw(false);
//
//            boolean checkmate = false;
//            if (com.crochess.engine0x88.GameState.isCheckmate(com.crochess.engine0x88.GameState.activeColor)) {
//                checkmate = true;
//                game.setResult("mate");
//                game.setWinner(moveDetails.playerId);
//            } else if (com.crochess.engine0x88.GameState.isForcedDraw()) {
//                game.setResult("draw");
//            } else if (com.crochess.engine0x88.GameState.isUnforcedDraw()) {
//                gs.setB_draw(true);
//                gs.setW_draw(true);
//            }
//
//            game.setMoves(game.getMoves() + moveDetails.notation);
//            String notation = com.crochess.engine0x88.GameState.createMoveNotation(move, captureDetails, checkmate);
//            String history = gs.getHistory() == null ? notation : gs.getHistory() + notation;
//            gs.setHistory(history);
//            gs.setFen(com.crochess.engine0x88.GameState.getFen());
//
//            // deal with timer
//            long timeSpent = System.currentTimeMillis() - gs.getTime_stamp_at_turn_start();
//            if (Objects.equals(moveDetails.playerId, game.getW_id())) {
//                long base = gs.getW_time() - timeSpent;
//                // convert increment to milliseconds before adding to base
//                gs.setW_time(base + (game.getIncrement() * 1000L));
//            } else {
//                long base = gs.getB_time() - timeSpent;
//                gs.setB_time(base + (game.getIncrement() * 1000L));
//            }
//            gs.setTime_stamp_at_turn_start(System.currentTimeMillis());
//        };
//
//        Game g = this.dao.update(id, patchGame);
//        return g.getGameState();
//    }
}
