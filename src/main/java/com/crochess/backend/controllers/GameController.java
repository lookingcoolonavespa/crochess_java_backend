package com.crochess.backend.controllers;

import com.crochess.backend.daos.GameDao;
import com.crochess.backend.misc.GameOverDetails;
import com.crochess.backend.misc.WsMessage;
import com.crochess.backend.models.DrawRecord;
import com.crochess.backend.models.Game;
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

    private void makeEngineMove(Game game, int depth) {
        com.crochess.moveValidator.Game moveValidator = new com.crochess.moveValidator.Game();
        int move;
        synchronized (GameController.class) {
            // dont want to load fen because you wont be able to detect draw by repetition
            String moves = game.getMoves() == null ? "" : " moves " + game.getMoves();
            com.crochess.engine0x88.Uci.inputPosition("position startpos" + moves);
            // update the state so you don't have to input the position in moveValidator as well
            moveValidator.board = com.crochess.engine0x88.GameState.board;
            moveValidator.pieceList = com.crochess.engine0x88.GameState.pieceList;
            moveValidator.activeColor = com.crochess.engine0x88.GameState.activeColor;
            moveValidator.castleRights = com.crochess.engine0x88.GameState.castleRights;
            moveValidator.totalRepititions = com.crochess.engine0x88.GameState.totalRepititions;
            moveValidator.enPassant = com.crochess.engine0x88.GameState.enPassant;
            moveValidator.zobristHash = com.crochess.engine0x88.GameState.zobristHash;

            move = com.crochess.engine0x88.moveEval.MoveEval.getBestMove(depth);
        }

        int captureDetails = moveValidator.makeMove(move);

        // dont need to reset draw record first so human player has a chance to accept unforced draw
        boolean checkmate = moveValidator.isCheckmate(moveValidator.activeColor);
        if (checkmate) {
            game.setResult("mate");
            char winner = Objects.equals("engine", game.getW_id()) ? 'w' : 'b';
            game.setWinner(winner);
        } else if (moveValidator.isForcedDraw()) {
            game.setResult("draw");
        } else if (moveValidator.isUnforcedDraw()) {
            DrawRecord dr = game.getDrawRecord();
            dr.setB(true);
            dr.setW(true);
        }

        String notation = moveValidator.createMoveNotation(move, captureDetails, checkmate);
        String history = game.getHistory() == null ? notation : game.getHistory() + " " + notation;
        game.setHistory(history);
        game.setFen(moveValidator.getFen());
        String algebra = com.crochess.engine0x88.Uci.moveToAlgebra(move);
        String moves = game.getMoves() == null ? algebra : game.getMoves() + " " + algebra;
        game.setMoves(moves);
    }

    @SubscribeMapping("/api/game/{id}")
    public void get(
            @DestinationVariable
            int id) throws JsonProcessingException {
        Game game = this.dao.get(id);
        this.template.convertAndSend("/topic/api/game/" + id, game.toJson("init"));

        if (Objects.equals(game.getW_id(), "engine")) {
            makeEngineMove(game, 4);
            this.dao.update(game);
            this.template.convertAndSend("/topic/api/game/" + id, game.toJson("game over"));
        }
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
    public void makeMove(
            @DestinationVariable
            int id,
            @Payload
            MoveDetails moveDetails) throws JsonProcessingException {
        Consumer<Game> patchGame = (Game game) -> {
            com.crochess.moveValidator.Game moveValidator = new com.crochess.moveValidator.Game();
            if (game.getResult() != null) return;
            if (game.getMoves() == null) com.crochess.engine0x88.Uci.inputPosition("position startpos", moveValidator);
            else {
                com.crochess.engine0x88.Uci.inputPosition("position startpos moves " + game.getMoves(), moveValidator);
            }
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
            if (checkmate) {
                game.setResult("mate");
                char winner = Objects.equals(moveDetails.playerId, game.getW_id()) ? 'w' : 'b';
                game.setWinner(winner);
            } else if (moveValidator.isForcedDraw()) {
                game.setResult("draw");
            } else if (moveValidator.isUnforcedDraw()) {
                dr.setB(true);
                dr.setW(true);
            }

            game.setMoves(game.getMoves() != null ? game.getMoves() + " " + moveDetails.move : moveDetails.move);
            String notation = moveValidator.createMoveNotation(move, captureDetails, checkmate);
            String history = game.getHistory() == null ? notation : game.getHistory() + " " + notation;
            game.setHistory(history);
            game.setFen(moveValidator.getFen());

            boolean playingAgainstEngine = Objects.equals(game.getW_id(), "engine") ||
                    Objects.equals(game.getB_id(), "engine");
            if (!playingAgainstEngine) {
                // deal with timer
                long timeSpent = System.currentTimeMillis() - game.getTime_stamp_at_turn_start();
                if (Objects.equals(moveDetails.playerId, game.getW_id())) {
                    long base = game.getW_time() - timeSpent;
                    // convert increment to milliseconds before adding to base
                    game.setW_time(base + (game.getIncrement() * 1000L));
                } else {
                    long base = game.getB_time() - timeSpent;
                    game.setB_time(base + (game.getIncrement() * 1000L));
                }
                game.setTime_stamp_at_turn_start(System.currentTimeMillis());
            }
        };
        Game g = this.dao.update(id, patchGame);

        if (g.getResult() != null) {
            this.template.convertAndSend("/topic/api/game/" + id, g.toJson("game over"));
            return;
        }
        this.template.convertAndSend("/topic/api/game/" + id, g.toJson("update"));

        boolean playingAgainstEngine = Objects.equals(g.getW_id(), "engine") ||
                Objects.equals(g.getB_id(), "engine");
        if (playingAgainstEngine) {
            int totalMoves = g.getMoves()
                              .split(" ").length;
            makeEngineMove(g, totalMoves > 1 ? 5 : 4);
            this.dao.update(g);
            if (g.getResult() != null) {
                this.template.convertAndSend("/topic/api/game/" + id, g.toJson("game over"));
                return;
            }
            this.template.convertAndSend("/topic/api/game/" + id, g.toJson("update"));
        }
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

            g.setResult(details.getResult());
            g.setWinner(details.getWinner());
            // deal with timer
            String currentTurn = g.getFen()
                                  .split(" ")[1];

            long timeSpent = System.currentTimeMillis() - g.getTime_stamp_at_turn_start();
            if (Objects.equals(currentTurn, "w")) {
                long base = g.getW_time() - timeSpent;
                // convert increment to milliseconds before adding to base
                g.setW_time(base + (g.getIncrement() * 1000L));
            } else {
                long base = g.getB_time() - timeSpent;
                g.setB_time(base + (g.getIncrement() * 1000L));
            }
            g.setTime_stamp_at_turn_start(System.currentTimeMillis());
        };
        Game g = this.dao.update(id, patchGame);

        this.template.convertAndSend("/topic/api/game/" + id, g.toJson("game over"));
    }
}
