package com.agnostik.agnostik_app.presence.events;


import com.agnostik.agnostik_app.dto.MoveResultDTO;
import com.agnostik.agnostik_app.dto.NeighborsDTO;
import com.agnostik.agnostik_app.presence.messaging.PresenceEventPublisher;
import com.agnostik.agnostik_app.service.FriendshipService;
import com.agnostik.agnostik_app.service.NeighborTextStoreService;
import com.agnostik.agnostik_app.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PresenceMoveNotifier {

    private final PresenceService presenceService;
    private final PresenceEventPublisher presenceEventPublisher;
    private final NeighborTextStoreService neighborTextStoreService;
    private final FriendshipService friendshipService;

    public void notifyMove(long moverId, MoveResultDTO result, NeighborsDTO before){
        if (result == null) return;

        NeighborsDTO after = presenceService.getNeighbors(moverId);
        Set<Long> targetIds = collectTargets(moverId, before, after);

        for (Long userId : targetIds) {
            if (userId == null) continue;
            Map<String, Object> payload = neighborsUpdatedPayload(userId);
            presenceEventPublisher.sendToUser(userId, "/queue/presence", payload);
        }
    }


    private Set<Long> collectTargets(long moverId, NeighborsDTO before, NeighborsDTO after){
        Set<Long> targets = new LinkedHashSet<>();
        addIfNotNull(targets, moverId);
        addIfNotNull(targets, before.getLeftUserId());
        addIfNotNull(targets, before.getRightUserId());
        addIfNotNull(targets, after.getLeftUserId());
        addIfNotNull(targets, after.getRightUserId());
        return targets;
    }

    private void addIfNotNull(Set<Long> set, Long value){
        if (value != null) set.add(value);
    }

    private Map<String, Object> neighborsUpdatedPayload(Long userId){
        NeighborsDTO neighbors = presenceService.getNeighbors(userId);
        int myIndex = presenceService.getMyIndex(userId);
        int corridorSize = presenceService.getCorridorSize();

        Map<String, Object> left =  neighborModel(userId, neighbors.getLeftUserId(), neighbors.isLeftLocked());
        Map<String, Object> right =  neighborModel(userId, neighbors.getRightUserId(), neighbors.isRightLocked());

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
        String text = neighborTextStoreService.getText(neighborId);
        return Map.of(
                "userId", neighborId,
                "locked", locked,
                "friend", friend,
                "text", text
        );
    }
}
