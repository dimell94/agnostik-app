package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.ResponseMessageDTO;
import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.service.EphemeralRequestService;
import com.agnostik.agnostik_app.service.FriendshipService;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class EphemeralRequestsRestController {

    private final EphemeralRequestService ephemeralRequestService;
    private final FriendshipService friendshipService;
    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;

    @PostMapping("/send/{direction}")
    public ResponseEntity<?> sendRequest(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable String direction){
        Long targetId = resolveNeighborId(me.getId(), direction);
        if (targetId == null){
            return ResponseEntity.badRequest().body(new ResponseMessageDTO("NO_NEIGHBOR_FOUND", "No neighbor found on " + direction));
        }
        ephemeralRequestService.send(me.getId(), targetId);
        snapshotNotifierService.notifyUsers(Set.of(me.getId(), targetId));

        return ResponseEntity.ok().body("Friend request sent from user: " + me.getId() + " to user: " + targetId);
    }

    @PostMapping("/cancel/{direction}")
    public ResponseEntity<?> cancelRequest(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable String direction){
        Long targetId = resolveNeighborId(me.getId(), direction);
        if (targetId == null) {
            return ResponseEntity.badRequest().body(new ResponseMessageDTO("NO_NEIGHBOR_FOUND", "No neighbor found on " + direction));
        }
        ephemeralRequestService.cancel(me.getId(), targetId);
        snapshotNotifierService.notifyUsers(Set.of(me.getId(), targetId));

        return ResponseEntity.ok().body("Friend request cancelled from user: " + me.getId() + " to user: " + targetId);
    }



    @PostMapping("/accept/{direction}")
    public ResponseEntity<?> acceptRequest(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable String direction){

        Long targetId = resolveNeighborId(me.getId(), direction);
        if (targetId == null){
            return ResponseEntity.badRequest().body(new ResponseMessageDTO("NO_NEIGHBOR_FOUND", "No neighbor found on " + direction));
        }

        ephemeralRequestService.cancel(targetId, me.getId());
        friendshipService.createFriendship(me.getId(), targetId);

        presenceService.lock(me.getId());
        presenceService.lock(targetId);

        snapshotNotifierService.notifyUsers(Set.of(me.getId(), targetId));


        return ResponseEntity.ok().body("Friendship created between users: " + me.getId() + ", " + targetId);
    }

    @PostMapping("/reject/{direction}")
    public ResponseEntity<?> rejectRequest(
            @AuthenticationPrincipal UserReadOnlyDTO me,
            @PathVariable String direction) {

        Long targetId = resolveNeighborId(me.getId(), direction);
        if (targetId == null)
            return ResponseEntity.badRequest().body(new ResponseMessageDTO("NO_NEIGHBOR_FOUND", "No neighbor found on " + direction));

        ephemeralRequestService.cancel(targetId, me.getId());
        snapshotNotifierService.notifyUsers(Set.of(me.getId(), targetId));
        return ResponseEntity.ok().body("Friend request from user: " + targetId + " to user: " + me.getId() + " was rejected");
    }



        private Long resolveNeighborId(Long meId, String direction) {
        var neighbors = presenceService.getNeighbors(meId);
        if ("left".equalsIgnoreCase(direction)) return neighbors.getLeftUserId();
        if ("right".equalsIgnoreCase(direction)) return neighbors.getRightUserId();
        return null;
    }




}
