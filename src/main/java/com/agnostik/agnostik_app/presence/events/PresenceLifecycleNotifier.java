
package com.agnostik.agnostik_app.presence.events;

import com.agnostik.agnostik_app.dto.NeighborsDTO;
import com.agnostik.agnostik_app.presence.messaging.PresenceEventPublisher;
import com.agnostik.agnostik_app.service.FriendshipService;
import com.agnostik.agnostik_app.service.NeighborTextStoreService;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PresenceLifecycleNotifier {

    private final PresenceService presenceService;
    private final SnapshotService snapshotService;
    private final PresenceEventPublisher publisher;
    private final PresenceMoveNotifier moveNotifier;
    private final FriendshipService friendshipService;
    private final NeighborTextStoreService textStoreService;

    public void notifyJoin(long userId) {

        publisher.sendToUser(userId, "/queue/presence", snapshotService.getSnapshot(userId));


        publisher.sendToUser(userId, "/queue/presence", neighborsUpdatedPayload(userId));


        NeighborsDTO neighbors = presenceService.getNeighbors(userId);
        notifyNeighbor(neighbors.getLeftUserId());
        notifyNeighbor(neighbors.getRightUserId());
    }

    public void notifyLeave(long userId, NeighborsDTO previousNeighbors) {
        if (previousNeighbors == null) {
            return;
        }

        notifyNeighbor(previousNeighbors.getLeftUserId());
        notifyNeighbor(previousNeighbors.getRightUserId());
    }

    private void notifyNeighbor(Long neighborId) {
        if (neighborId == null) return;
        publisher.sendToUser(neighborId, "/queue/presence", neighborsUpdatedPayload(neighborId));
    }

    private Map<String, Object> neighborsUpdatedPayload(Long userId) {
        if (userId == null) return null;

        NeighborsDTO neighbors = presenceService.getNeighbors(userId);
        int myIndex = presenceService.getMyIndex(userId);
        int corridorSize = presenceService.getCorridorSize();

        Map<String, Object> left  = neighborModel(userId, neighbors.getLeftUserId(), neighbors.isLeftLocked());
        Map<String, Object> right = neighborModel(userId, neighbors.getRightUserId(), neighbors.isRightLocked());

        Map<String, Object> data = Map.of(
                "myIndex", myIndex,
                "corridorSize", corridorSize,
                "left", left,
                "right", right
        );
        return Map.of("type", "NEIGHBORS_UPDATED", "data", data);
    }

    private Map<String, Object> neighborModel(Long ownerId, Long neighborId, boolean locked) {
        if (neighborId == null) return null;
        boolean friend = friendshipService.areFriends(ownerId, neighborId);
        String text = textStoreService.getText(neighborId);
        return Map.of(
                "userId", neighborId,
                "locked", locked,
                "friend", friend,
                "text", text
        );
    }

}

