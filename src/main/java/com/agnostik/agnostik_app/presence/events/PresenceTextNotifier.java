package com.agnostik.agnostik_app.presence.events;


import com.agnostik.agnostik_app.presence.messaging.PresenceEventPublisher;
import com.agnostik.agnostik_app.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceTextNotifier {

    private final PresenceService presenceService;
    private final PresenceEventPublisher eventPublisher;

    public void notifyTextUpdate (long userId, String text) {
        var neighbors = presenceService.getNeighbors(userId);
        Map<String, Object> payload = Map.of(
                "type", "NEIGHBOR_TEXT_UPDATED",
                "data", Map.of("userId", userId, "text", text)
        );

        sendIfPresent(neighbors.getLeftUserId(), payload);
        sendIfPresent(neighbors.getRightUserId(), payload);
    }

    private void sendIfPresent(Long neighborId, Object payload){
        if (neighborId != null){
            eventPublisher.sendToUser(neighborId, "/queue/presence", payload);
        }
    }
}
