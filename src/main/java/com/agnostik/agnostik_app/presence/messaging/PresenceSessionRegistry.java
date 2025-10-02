package com.agnostik.agnostik_app.presence.messaging;


import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceSessionRegistry {

    @Getter
    private final Map<String, Long> sessionsToUser = new ConcurrentHashMap<>();

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal instanceof UserReadOnlyDTO user){
            sessionsToUser.put(accessor.getSessionId(), user.getId());
            log.info("WS session {} connected for user {}", accessor.getSessionId(), user.getId());
        }else {
            log.warn("WS session {} missing authenticated user, disconnecting", accessor.getSessionId());
        }
    }

    @EventListener
    public void handleSessionDisconected(SessionDisconnectEvent event){
        String sessionId = event.getSessionId();
        Long userId = sessionsToUser.remove(sessionId);
        log.info("WS session {} disconnected (user: {})", sessionId, userId);
    }
}
