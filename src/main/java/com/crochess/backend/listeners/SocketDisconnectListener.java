package com.crochess.backend.listeners;

import com.crochess.backend.misc.WsMessage;
import com.crochess.backend.repositories.GameSeekRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Objects;

@Transactional
@Component
public class SocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    GameSeekRepository gsRepo;
    SimpMessagingTemplate template;

    public SocketDisconnectListener(GameSeekRepository gsRepo, SimpMessagingTemplate template) {
        this.gsRepo = gsRepo;
        this.template = template;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String username = Objects.requireNonNull(event.getUser())
                                 .getName();
        List<Integer> deletedSeeks = this.gsRepo.getSeeksBySeeker(username);
        this.gsRepo.deleteSeeksBySeeker(username);

        String json = null;
        try {
            json = new ObjectMapper().writeValueAsString(new WsMessage<List<Integer>>("delete", deletedSeeks));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        this.template.convertAndSend("/topic/api/gameseeks", json);
    }
}
