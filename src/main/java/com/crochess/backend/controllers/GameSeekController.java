package com.crochess.backend.controllers;

import com.crochess.backend.misc.WsMessage;
import com.crochess.backend.models.gameSeek.GameSeek;
import com.crochess.backend.repositories.GameSeekRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/gameseeks")
@RestController
public class GameSeekController {
    private final GameSeekRepository repository;
    private final SimpMessagingTemplate template;


    public GameSeekController(GameSeekRepository repository, SimpMessagingTemplate template) {
        this.repository = repository;
        this.template = template;
    }

    @MessageMapping("/api/gameseeks")
    void newGameSeek(
            Message<GameSeek> message) throws JsonProcessingException {
        GameSeek newGs = message.getPayload();
        repository.save(newGs);
        String json = new ObjectMapper().writeValueAsString(new WsMessage<GameSeek>("insert", newGs));
        this.template.convertAndSend("/topic/api/gameseeks", json);
    }

    @DeleteMapping("/{id}")
    void deleteById(
            @PathVariable
            int id) {
        repository.deleteById(id);
    }

    @SubscribeMapping("/api/gameseeks")
    String getAll() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(repository.findAll());
    }
}
