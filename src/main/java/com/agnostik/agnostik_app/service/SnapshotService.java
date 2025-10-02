package com.agnostik.agnostik_app.service;


import com.agnostik.agnostik_app.dto.NeighborsDTO;
import com.agnostik.agnostik_app.dto.SnapshotDTO;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final EphemeralRequestService ephemeralRequestService;
    private final FriendshipService friendshipService;
    private final PresenceService presenceService;
    private final UserRepository userRepository;
    private final NeighborTextStoreService neighborTextStoreService;

    public SnapshotDTO getSnapshot(Long userId){
        User me = userRepository.findById(userId).orElse(null);

        int myIndex = presenceService.getMyIndex(userId);
        int corridorSize = presenceService.getCorridorSize();
        NeighborsDTO neighbors = presenceService.getNeighbors(userId);

        Long leftId = neighbors.getLeftUserId();
        Long rightId = neighbors.getRightUserId();

        List<Long> outgoing = new ArrayList<>();
        if (leftId != null && ephemeralRequestService.hasOutgoing(userId,leftId)){
            outgoing.add(leftId);
        }
        if (rightId != null && ephemeralRequestService.hasOutgoing(userId,rightId)){
            outgoing.add(rightId);
        }

        List<Long> incoming = new ArrayList<>();
        if (leftId != null && ephemeralRequestService.hasIncoming(userId,leftId)){
            incoming.add(leftId);
        }
        if (rightId != null && ephemeralRequestService.hasIncoming(userId,rightId)){
            incoming.add(rightId);
        }

        return SnapshotDTO.builder()
                .meId(me != null ? me.getId() : userId)
                .meUsername(me != null ? me.getUsername(): null)
                .myIndex(myIndex)
                .corridorSize(corridorSize)
                .locked(presenceService.isLocked(userId))
                .leftUserId(leftId)
                .leftLocked(leftId != null ? neighbors.isLeftLocked(): null)
                .leftFriend(leftId != null ? friendshipService.areFriends(userId, leftId) : null)
                .leftText(leftId != null ? neighborTextStoreService.getText(leftId) : null)
                .rightUserId(rightId)
                .rightLocked(rightId != null ? neighbors.isRightLocked(): null)
                .rightFriend(rightId != null ? friendshipService.areFriends(userId, rightId) : null)
                .rightText(rightId != null ? neighborTextStoreService.getText(rightId) : null)
                .requestsOutgoing(outgoing)
                .requestsIncoming(incoming)
                .build();


    }
}
