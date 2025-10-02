
package com.agnostik.agnostik_app.presence.events;

import com.agnostik.agnostik_app.presence.messaging.PresenceEventPublisher;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PresenceLifecycleNotifier {

    private final PresenceService presenceService;
    private final SnapshotService snapshotService;
    private final PresenceEventPublisher publisher;
    private final PresenceMoveNotifier moveNotifier;

    public void notifyJoin(long userId) {
        sendSnapshot(userId);
        var neighbors = presenceService.getNeighbors(userId);
        moveNotifier.notifyMove(userId, null, neighbors);
    }

    public void notifyLeave(long userId) {
        var neighbors = presenceService.getNeighbors(userId);
        moveNotifier.notifyMove(userId, null, neighbors);
    }

    public void sendSnapshot(long userId) {
        publisher.sendToUser(userId, "/queue/presence", snapshotService.getSnapshot(userId));
    }
}

