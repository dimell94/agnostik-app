package com.agnostik.agnostik_app.presence.messaging;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PresenceEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(Object payload){
        messagingTemplate.convertAndSend("/topic/presence", payload);
    }

    public void sendToUser(Long userId, String destinationSuffix, Object payload){
        if (userId == null) return;
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                destinationSuffix,
                payload
        );
    }

    public void sendToNeighborTopic (Long userId, Object payload){
        messagingTemplate.convertAndSend("/topic/neighbors" + userId, payload);
    }


}
