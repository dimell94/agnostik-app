package com.agnostik.agnostik_app.api;

import com.agnostik.agnostik_app.dto.ResponseMessageDTO;
import com.agnostik.agnostik_app.dto.UserReadOnlyDTO;
import com.agnostik.agnostik_app.service.EphemeralRequestService;
import com.agnostik.agnostik_app.service.FriendshipService;
import com.agnostik.agnostik_app.service.PresenceService;
import com.agnostik.agnostik_app.service.SnapshotNotifierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Requests", description = "Friend request commands; UI updates via WebSocket events")
public class EphemeralRequestsRestController {

    private final EphemeralRequestService ephemeralRequestService;
    private final FriendshipService friendshipService;
    private final PresenceService presenceService;
    private final SnapshotNotifierService snapshotNotifierService;

    @PostMapping("/send/{direction}")
    @Operation(
            summary = "Send friend request",
            description = "Sends a friend request to left/right neighbor. State changes are observed via WebSocket events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request sent"),
                    @ApiResponse(responseCode = "400", description = "No neighbor found",
                            content = @Content(schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
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
    @Operation(
            summary = "Cancel friend request",
            description = "Cancels an outgoing friend request. UI syncs via WebSocket events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request cancelled"),
                    @ApiResponse(responseCode = "400", description = "No neighbor found",
                            content = @Content(schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
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
    @Operation(
            summary = "Accept friend request",
            description = "Accepts incoming request, creates friendship, locks both users. State via WebSocket events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Friendship created"),
                    @ApiResponse(responseCode = "400", description = "No neighbor found",
                            content = @Content(schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
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
    @Operation(
            summary = "Reject friend request",
            description = "Rejects incoming request. UI state via WebSocket events.",
            security = { @SecurityRequirement(name = "bearer-jwt") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request rejected"),
                    @ApiResponse(responseCode = "400", description = "No neighbor found",
                            content = @Content(schema = @Schema(implementation = ResponseMessageDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
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
