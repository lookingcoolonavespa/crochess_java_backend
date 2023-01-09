package com.crochess.backend.controllers;

import com.crochess.backend.daos.GameDao;
import com.crochess.backend.misc.WsMessage;
import com.crochess.backend.models.DrawRecord;
import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameState;
import com.crochess.engine0x88.types.Color;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
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

    @SubscribeMapping("/api/game/{id}")
    public Game get(
            @DestinationVariable
            int id) {
        return this.dao.get(id);
    }


    @AllArgsConstructor
    private static class GameIdMsg {
        public Integer gameId;
        public String playerColor;
    }

    @MessageMapping("/api/game")
    public Game insert(
            Message<Game>
                    message) throws JsonProcessingException {
        Game game = message.getPayload();
        List<Integer> deletedGameSeeks = this.dao.insert(game);
        String json = new ObjectMapper().writeValueAsString(new WsMessage<List<Integer>>("delete", deletedGameSeeks));
        this.template.convertAndSend("/topic/api/gameseeks", json);
        String wJson = new ObjectMapper().writeValueAsString(new GameIdMsg(game.getId(), "w"));
        String bJson = new ObjectMapper().writeValueAsString(new GameIdMsg(game.getId(), "b"));
        template.convertAndSendToUser(game.getW_id(), "/queue/gameseeks", wJson);
        template.convertAndSendToUser(game.getB_id(), "/queue/gameseeks", bJson);
        return game;
    }

    @AllArgsConstructor
    public static class MoveDetails {
        public String playerId;
        public String move;
    }

    @MessageMapping("/api/game/{id}")
    public String makeMove(
            @DestinationVariable
            int id,
            @Payload
            MoveDetails moveDetails) throws JsonProcessingException {
        Consumer<Game> patchGame = (Game game) -> {
            GameState gs = game.getGameState();
            System.out.println(gs);
            if (gs.getWinner() != null) return;

            if (gs.getMoves() == null) com.crochess.engine0x88.Uci.inputPosition("position startpos");
            else com.crochess.engine0x88.Uci.inputPosition("position startpos moves " + gs.getMoves());
            // if the playerId doesn't match up to the active color
            if (com.crochess.engine0x88.GameState.activeColor == Color.W &&
                    !Objects.equals(moveDetails.playerId, game.getW_id()) ||
                    com.crochess.engine0x88.GameState.activeColor == Color.B &&
                            !Objects.equals(moveDetails.playerId, game.getB_id()))
                return;

            int move = com.crochess.engine0x88.Uci.algebraToMove(moveDetails.move);
            if (!com.crochess.engine0x88.GameState.isMoveValid(move)) return;
            int captureDetails = com.crochess.engine0x88.GameState.makeMove(move);

            // player loses right to claim draw after move is made
            DrawRecord dr = game.getDrawRecord();
            dr.setB(false);
            dr.setW(false);

            boolean checkmate = false;
            if (com.crochess.engine0x88.GameState.isCheckmate(com.crochess.engine0x88.GameState.activeColor)) {
                checkmate = true;
                gs.setResult("mate");
                gs.setWinner(moveDetails.playerId);
            } else if (com.crochess.engine0x88.GameState.isForcedDraw()) {
                gs.setResult("draw");
            } else if (com.crochess.engine0x88.GameState.isUnforcedDraw()) {
                dr.setB(true);
                dr.setW(true);
            }

            gs.setMoves(gs.getMoves() != null ? gs.getMoves() + " " + moveDetails.move : moveDetails.move);
            String notation = com.crochess.engine0x88.GameState.createMoveNotation(move, captureDetails, checkmate);
            String history = gs.getHistory() == null ? notation : gs.getHistory() + " " + notation;
            gs.setHistory(history);
            gs.setFen(com.crochess.engine0x88.GameState.getFen());

            // deal with timer
            long timeSpent = System.currentTimeMillis() - gs.getTime_stamp_at_turn_start();
            if (Objects.equals(moveDetails.playerId, game.getW_id())) {
                long base = gs.getW_time() - timeSpent;
                // convert increment to milliseconds before adding to base
                gs.setW_time(base + (game.getIncrement() * 1000L));
            } else {
                long base = gs.getB_time() - timeSpent;
                gs.setB_time(base + (game.getIncrement() * 1000L));
            }
            gs.setTime_stamp_at_turn_start(System.currentTimeMillis());
        };

        Game g = this.dao.update(id, patchGame);

        return new ObjectMapper().writeValueAsString(new WsMessage<GameState>("update", g.getGameState()));
    }
}
