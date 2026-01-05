package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.ResponseMessageDTO;
import com.agnostik.agnostik_app.dto.SnapshotDTO;
import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
import com.agnostik.agnostik_app.service.SnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.stream.Stream;

@RestController
@RequestMapping("api/presence/")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Presence", description = "Snapshot and presence commands (state is reflected via WebSocket events)")
public class PresenceRestController {

    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;
    private final SnapshotService snapshotService;

    @PostMapping("/leave")
    @Operation(
            summary = "Leave corridor",
            description = "User leaves presence. UI updates from subsequent WebSocket snapshot events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Left corridor"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> leave(@AuthenticationPrincipal UserReadOnlyDTO me){
        var before = presenceService.getNeighbors(me.getId());
        presenceService.leave(me.getId());
        notifyMeAndNeighbors(me.getId(), before.getLeftUserId(), before.getRightUserId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " left");

    }

    @PostMapping("/lock")
    @Operation(
            summary = "Lock position",
            description = "Locks current position. UI updates from WebSocket events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Locked"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> lock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.lock(me.getId());
        var neighbors = presenceService.getNeighbors(me.getId());
        notifyMeAndNeighbors(me.getId(), neighbors.getLeftUserId(), neighbors.getRightUserId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " locked position");
    }

    @PostMapping("/unlock")
    @Operation(
            summary = "Unlock position",
            description = "Unlocks current position. UI updates from WebSocket events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unlocked"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> unlock(@AuthenticationPrincipal UserReadOnlyDTO me){
        presenceService.unlock(me.getId());
        var neighbors = presenceService.getNeighbors(me.getId());
        notifyMeAndNeighbors(me.getId(), neighbors.getLeftUserId(), neighbors.getRightUserId());
        return ResponseEntity.ok().body("User with id: " + me.getId() + " unlocked position");
    }

    @PostMapping("/moveLeft")
    @Operation(
            summary = "Move left",
            description = "Attempts to move left. Authoritative state will arrive via WebSocket snapshot/events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Move result"),
                    @ApiResponse(responseCode = "409", description = "Cannot move left",
                            content = @Content(schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> moveLeft(@AuthenticationPrincipal UserReadOnlyDTO me){
        var before = presenceService.getNeighbors(me.getId());

        var moveResult = presenceService.moveLeft(me.getId());

        if (moveResult == null){
            return ResponseEntity.status(409)
                    .body(new ResponseMessageDTO("CANNOT_MOVE_LEFT", "User cannot move left"));
        }

        var after = presenceService.getNeighbors(me.getId());

        Set<Long> impacted = Stream.of(
                        me.getId(),
                        before.getLeftUserId(),  before.getRightUserId(),
                        after.getLeftUserId(),   after.getRightUserId()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        snapshotNotifierService.notifyUsers(impacted);


        
        return ResponseEntity.ok(moveResult);
    }

    @PostMapping("/moveRight")
    @Operation(
            summary = "Move right",
            description = "Attempts to move right. Authoritative state will arrive via WebSocket snapshot/events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Move result"),
                    @ApiResponse(responseCode = "409", description = "Cannot move right",
                            content = @Content(schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<?> moveRight(@AuthenticationPrincipal UserReadOnlyDTO me){
        var before = presenceService.getNeighbors(me.getId());

        var moveResult = presenceService.moveRight(me.getId());
        if (moveResult == null){
            return ResponseEntity.status(409)
                    .body(new ResponseMessageDTO("CANNOT_MOVE_RIGHT", "User cannot move right"));
        }

        var after = presenceService.getNeighbors(me.getId());

        Set<Long> impacted = Stream.of(
                        me.getId(),
                        before.getLeftUserId(),  before.getRightUserId(),
                        after.getLeftUserId(),   after.getRightUserId()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        snapshotNotifierService.notifyUsers(impacted);



        return ResponseEntity.ok(moveResult);
    }

    private void notifyMeAndNeighbors (Long me, Long left, Long right) {
        Set<Long> ids = new HashSet<>();
        ids.add(me);
        if (left != null) ids.add(left);
        if (right != null) ids.add(right);
        snapshotNotifierService.notifyUsers(ids);
    }

    @GetMapping("/snapshot")
    @Operation(
            summary = "Get presence snapshot",
            description = "Initial/full snapshot for the authenticated user. Subsequent updates are delivered via WebSocket.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Snapshot returned",
                            content = @Content(schema = @Schema(implementation = SnapshotDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<SnapshotDTO> getSnapshot(@AuthenticationPrincipal UserReadOnlyDTO me) {
        SnapshotDTO snapshot = snapshotService.buildFor(me.getId());
        return ResponseEntity.ok(snapshot);
    }



}
