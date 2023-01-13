package com.crochess.backend.controllers;

import com.crochess.backend.daos.GameDao;
import com.crochess.backend.misc.WsMessage;
import com.crochess.backend.models.DrawRecord;
import com.crochess.backend.models.Game;
import com.crochess.backend.models.GameOverDetails;
import com.crochess.backend.models.GameState;
import com.crochess.backend.repositories.DrawRecordRepository;
import com.crochess.engine0x88.Uci;
import com.crochess.engine0x88.types.Color;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    private final DrawRecordRepository drRepo;

    public GameController(GameDao dao, SimpMessagingTemplate template, DrawRecordRepository drRepo) {
        this.dao = dao;
        this.template = template;
        this.drRepo = drRepo;
    }

    private void makeEngineMove(Game game) {
        com.crochess.moveValidator.Game moveValidator = new com.crochess.moveValidator.Game();
        int move;
        synchronized (GameController.class) {
            move = com.crochess.engine0x88.moveEval.MoveEval.getBestMove(6);
        }
        int captureDetails = moveValidator.makeMove(move);

        // dont need to reset draw record first so human player has a chance to accept unforced draw
        GameOverDetails details = game.getDetails();
        boolean checkmate = moveValidator.isCheckmate(moveValidator.activeColor);
        if (checkmate) {
            details.setResult("mate");
            char winner = Objects.equals("engine", game.getW_id()) ? 'w' : 'b';
            details.setWinner(winner);
        } else if (moveValidator.isForcedDraw()) {
            details.setResult("draw");
        } else if (moveValidator.isUnforcedDraw()) {
            DrawRecord dr = game.getDrawRecord();
            dr.setB(true);
            dr.setW(true);
        }

        GameState gs = game.getGameState();
        gs.setMoves(gs.getMoves() + " " + Uci.moveToAlgebra(move));
        String notation = moveValidator.createMoveNotation(move, captureDetails, checkmate);
        gs.setHistory(gs.getHistory() + " " + notation);
        gs.setFen(moveValidator.getFen());
    }

    @SubscribeMapping("/api/game/{id}")
    public Game get(
            @DestinationVariable
            int id) {
        Game game = this.dao.get(id);
        if (Objects.equals(game.getW_id(), "engine")) {
            makeEngineMove(game);
            this.dao.update(game);
        }
        return game;
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

    @Getter
    private static class GameOverGameState {
        public GameOverGameState(GameState gs, GameOverDetails details) {
            this.game_id = gs.getGame_id();
            this.time_stamp_at_turn_start = gs.getTime_stamp_at_turn_start();
            this.fen = gs.getFen();
            this.w_time = gs.getW_time();
            this.b_time = gs.getB_time();
            this.history = gs.getHistory();
            this.moves = gs.getMoves();
            this.result = details.getResult();
            this.winner = String.valueOf(details.getWinner());
        }

        private final Integer game_id;
        private final long time_stamp_at_turn_start;
        private final String fen;
        private final long w_time;
        private final long b_time;
        private final String history;
        private final String moves;
        private final String result; // mate, draw, or time
        private final String winner;
    }

    @MessageMapping("/api/game/{id}")
    public String makeMove(
            @DestinationVariable
            int id,
            @Payload
            MoveDetails moveDetails) throws JsonProcessingException {
        Consumer<Game> patchGame = (Game game) -> {
            com.crochess.moveValidator.Game moveValidator = new com.crochess.moveValidator.Game();
            GameState gs = game.getGameState();
            GameOverDetails details = game.getDetails();
            if (details.getResult() != null) return;

            if (gs.getMoves() == null) com.crochess.engine0x88.Uci.inputPosition("position startpos", moveValidator);
            else com.crochess.engine0x88.Uci.inputPosition("position startpos moves " + gs.getMoves(), moveValidator);
            // if the playerId doesn't match up to the active color
            if (moveValidator.activeColor == Color.W &&
                    !Objects.equals(moveDetails.playerId, game.getW_id()) ||
                    moveValidator.activeColor == Color.B &&
                            !Objects.equals(moveDetails.playerId, game.getB_id()))
                return;

            int move = com.crochess.engine0x88.Uci.algebraToMove(moveDetails.move);
            if (!moveValidator.isMoveValid(move)) return;
            int captureDetails = moveValidator.makeMove(move);

            // player loses right to claim draw after move is made
            DrawRecord dr = game.getDrawRecord();
            dr.setB(false);
            dr.setW(false);

            // need this variable for the creation of the move notation
            boolean checkmate =
                    moveValidator.isCheckmate(moveValidator.activeColor);
            // need this variable to see if engine (if other player is engine) needs to make a move
            boolean forcedDraw = false;
            if (checkmate) {
                details.setResult("mate");
                char winner = Objects.equals(moveDetails.playerId, game.getW_id()) ? 'w' : 'b';
                details.setWinner(winner);
            } else if (moveValidator.isForcedDraw()) {
                forcedDraw = true;
                details.setResult("draw");
            } else if (moveValidator.isUnforcedDraw()) {
                dr.setB(true);
                dr.setW(true);
            }

            gs.setMoves(gs.getMoves() != null ? gs.getMoves() + " " + moveDetails.move : moveDetails.move);
            String notation = moveValidator.createMoveNotation(move, captureDetails, checkmate);
            String history = gs.getHistory() == null ? notation : gs.getHistory() + " " + notation;
            gs.setHistory(history);
            gs.setFen(moveValidator.getFen());

            boolean playingAgainstEngine = Objects.equals(game.getW_id(), "engine") ||
                    Objects.equals(game.getB_id(), "engine");
            if (!playingAgainstEngine) {
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
            } else if (!checkmate && !forcedDraw) {
                makeEngineMove(game);
            }
        };
        Game g = this.dao.update(id, patchGame);

        if (g.getDetails()
             .getResult() != null) {
            return new ObjectMapper().writeValueAsString(new WsMessage<GameOverGameState>("game over",
                    new GameOverGameState(g.getGameState(), g.getDetails())));
        }
        return new ObjectMapper().writeValueAsString(new WsMessage<GameState>("update", g.getGameState()));
    }

    @MessageMapping("/api/game/{id}/draw")
    public void updateDraw(
            @DestinationVariable
            int id,
            @Payload
            DrawRecord dr) throws JsonProcessingException {
        dr.setGame_id(id);
        this.drRepo.save(dr);
        String json = new ObjectMapper().writeValueAsString(new WsMessage<DrawRecord>("update draw", dr));
        this.template.convertAndSend("/topic/api/game/" + id, json);
    }

    @MessageMapping("/api/game/{id}/resign-draw")
    public void resignOrClaimDraw(
            @DestinationVariable
            int id, GameOverDetails details) throws JsonProcessingException {

        Consumer<Game> patchGame = (Game g) -> {
            DrawRecord dr = g.getDrawRecord();
            dr.setB(false);
            dr.setW(false);

            details.setGame_id(id);
            g.setDetails(details);

            // deal with timer
            GameState gs = g.getGameState();
            String currentTurn = gs.getFen()
                                   .split(" ")[1];

            long timeSpent = System.currentTimeMillis() - gs.getTime_stamp_at_turn_start();
            if (Objects.equals(currentTurn, "w")) {
                long base = gs.getW_time() - timeSpent;
                // convert increment to milliseconds before adding to base
                gs.setW_time(base + (g.getIncrement() * 1000L));
            } else {
                long base = gs.getB_time() - timeSpent;
                gs.setB_time(base + (g.getIncrement() * 1000L));
            }
            gs.setTime_stamp_at_turn_start(System.currentTimeMillis());
        };
        Game g = this.dao.update(id, patchGame);

        String json = new ObjectMapper().writeValueAsString(new WsMessage<GameOverGameState>("game over",
                new GameOverGameState(g.getGameState(), g.getDetails())));

        this.template.convertAndSend("/topic/api/game/" + id, json);
    }
}
