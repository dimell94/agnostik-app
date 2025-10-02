package com.agnostik.agnostik_app.presence.events;

import com.agnostik.agnostik_app.presence.messaging.PresenceEventPublisher;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceLockNotifier {

    private final PresenceService presenceService;
    private final SnapshotService snapshotService;
    private final PresenceEventPublisher eventPublisher;

    public void notifyLockChange(long userId, boolean locked){
        var snapshot = snapshotService.getSnapshot(userId);
        eventPublisher.sendToUser(userId, "/queue/presence", snapshot);

        var neighbors = presenceService.getNeighbors(userId);
        sendLockEvent(neighbors.getLeftUserId(), userId, locked);
        sendLockEvent(neighbors.getRightUserId(), userId, locked);
    }

    private void sendLockEvent (Long neighborId, long userId, boolean locked){
        if (neighborId == null) return;
        Map<String, Object> payload = Map.of(
                "type", locked ? "USER_LOCKED" : "USER_UNLOCKED",
                "data", Map.of("userId", userId)
        );
        eventPublisher.sendToUser(neighborId, "/queue/presence", payload);
    }
}
