package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.model.User;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/presence/")
@RequiredArgsConstructor
@Slf4j
public class PresenceRestController {

    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;

    @PostMapping("/leave")
    public ResponseEntity<?> leave(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.leave(me.getId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " left");

    }

    @PostMapping("/lock")
    public ResponseEntity<?> lock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.lock(me.getId());
        var neighbors = presenceService.getNeighbors(me.getId());
        notifyMeAndNeighbors(me.getId(), neighbors.getLeftUserId(), neighbors.getRightUserId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " locked position");
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.unlock(me.getId());
        var neighbors = presenceService.getNeighbors(me.getId());
        notifyMeAndNeighbors(me.getId(), neighbors.getLeftUserId(), neighbors.getRightUserId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " unlocked position");
    }

    @PostMapping("/moveLeft")
    public ResponseEntity<?> moveLeft(@AuthenticationPrincipal UserReadOnlyDTO me){
        var before = presenceService.getNeighbors(me.getId());

        var moveResult = presenceService.moveLeft(me.getId());

        if (moveResult == null){
            return ResponseEntity.status(409).body("CANNOT_MOVE_LEFT");
        }

        var after = presenceService.getNeighbors(me.getId());

        Set<Long> impacted = Set.of(
                me.getId(),
                before.getLeftUserId(), before.getRightUserId(),
                after.getLeftUserId(), after.getRightUserId()
        ).stream().filter(Objects::nonNull).collect(Collectors.toSet());

        snapshotNotifierService.notifyUsers(impacted);


        // to build a response with move results
        return ResponseEntity.ok().body("User with id: " + me.getId() + " moved left");
    }

    @PostMapping("/moveRight")
    public ResponseEntity<?> moveRight(@AuthenticationPrincipal UserReadOnlyDTO me){
        var before = presenceService.getNeighbors(me.getId());

        var moveResult = presenceService.moveRight(me.getId());
        if (moveResult == null){
            return ResponseEntity.status(409).body("CANNOT_MOVE_RIGHT");
        }

        var after = presenceService.getNeighbors(me.getId());

        Set<Long> impacted = Set.of(
                me.getId(),
                before.getLeftUserId(), before.getRightUserId(),
                after.getLeftUserId(), after.getRightUserId()
        ).stream().filter(Objects::nonNull).collect(Collectors.toSet());

        snapshotNotifierService.notifyUsers(impacted);



        return ResponseEntity.ok().body("User with id: " + me.getId() + " moved right");
    }

    private void notifyMeAndNeighbors (Long me, Long left, Long right) {
        Set<Long> ids = new HashSet<>();
        ids.add(me);
        if (left != null) ids.add(left);
        if (right != null) ids.add(right);
        snapshotNotifierService.notifyUsers(ids);
    }


}
