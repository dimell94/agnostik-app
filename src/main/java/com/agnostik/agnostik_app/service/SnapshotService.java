package com.agnostik.agnostik_app.service;

import com.agnostik.agnostik_app.dto.SnapshotDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final PresenceService presenceService;
    private final FriendshipService friendshipService;
    private final EphemeralRequestService ephemeralRequestService;
    private final TextService textService;

    public SnapshotDTO buildFor (Long userId){
        var neighbors = presenceService.getNeighbors(userId);

        var me = SnapshotDTO.UserView.builder()
                .id(userId)
                .text(textService.getText(userId))
                .locked(presenceService.isLocked(userId))
                .myIndex(presenceService.getIndexOf(userId))
                .build();

        var leftId = neighbors.getLeftUserId();
        var left = (leftId != null) ? buildNeighborView(userId, leftId) : null;

        var rightId = neighbors.getRightUserId();
        var right = (rightId != null) ? buildNeighborView(userId,rightId) : null;

        var corridor = SnapshotDTO.CorridorInfo.builder()
                .size(presenceService.getCorridorSize())
                .build();

        return SnapshotDTO.builder()
                .me(me)
                .left(left)
                .right(right)
                .corridor(corridor)
                .build();

    }

    private SnapshotDTO.NeighborView buildNeighborView (Long meId, Long neighborId) {
        boolean friend = friendshipService.areFriends(meId, neighborId);
        boolean requestToMe = ephemeralRequestService.hasIncoming(meId, neighborId);
        boolean requestFromMe = ephemeralRequestService.hasOutgoing(meId, neighborId);

        return SnapshotDTO.NeighborView.builder()
                .id(neighborId)
                .locked(presenceService.isLocked(neighborId))
                .text(textService.getText(neighborId))
                .friend(friend)
                .requestToMe(requestToMe)
                .requestFromMe(requestFromMe)
                .build();
    }




}
